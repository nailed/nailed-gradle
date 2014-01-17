package jk_5.nailed.gradle.tasks;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.jcraft.jsch.*;
import groovy.lang.Closure;
import jk_5.nailed.gradle.Constants;
import jk_5.nailed.gradle.delayed.DelayedString;
import jk_5.nailed.gradle.extension.NailedExtension;
import org.apache.tools.ant.filters.StringInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * No description given
 *
 * @author jk-5
 */
public class DeployLauncherProfileTask extends DefaultTask {

    public DeployLauncherProfileTask() {
        super();

        this.onlyIf(new Closure<Boolean>(this, this){
            @Override
            public Boolean call(Object... objects) {
                NailedExtension ext = NailedExtension.getInstance(DeployLauncherProfileTask.this.getProject());
                String host = ext.getDeployHost();
                String username = ext.getDeployUsername();
                String password = ext.getDeployPassword();
                return !(Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password));
            }
        });
    }

    @TaskAction
    public void doTask() throws IOException, JSchException, SftpException {
        NailedExtension ext = NailedExtension.getInstance(this.getProject());
        URL url = new URL(new DelayedString(this.getProject(), Constants.FML_JSON_URL).call());
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        Reader reader = new InputStreamReader(conn.getInputStream());
        JsonElement parsed = new JsonParser().parse(reader);
        JsonObject remoteInfo = parsed.getAsJsonObject().getAsJsonObject("versionInfo");
        JsonArray libs = remoteInfo.getAsJsonArray("libraries");
        JsonArray newArray = new JsonArray();
        for(JsonObject dep : ext.getLauncherDependencies()){
            if(dep.has("url") && dep.get("url").getAsString().equals("{MAVEN_URL}")){
                dep.remove("url");
                dep.addProperty("url", ext.getLoadingMavenUrl());
            }
            newArray.add(dep);
        }
        for(JsonElement element : libs){
            JsonObject object = element.getAsJsonObject();
            if(!object.get("name").getAsString().equals("@artifact@")){
                JsonObject newObject = new JsonObject();
                newObject.addProperty("name", object.get("name").getAsString());
                if(object.has("url")) newObject.add("url", object.get("url"));
                if(object.has("rules")) newObject.add("rules", object.get("rules"));
                if(object.has("extract")) newObject.add("extract", object.get("extract"));
                if(object.has("natives")) newObject.add("natives", object.get("natives"));
                newArray.add(newObject);
            }
        }
        reader.close();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        StringBuilder argsBuilder = new StringBuilder("--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type}");
        for(String string : ext.getTweakers()){
            argsBuilder.append(" --tweakClass ");
            argsBuilder.append(string);
        }

        JsonObject newRoot = new JsonObject();
        newRoot.addProperty("id", ext.getVersionName());
        newRoot.addProperty("mainClass", ext.getMainClass());
        newRoot.addProperty("minimumLauncherVersion", remoteInfo.get("minimumLauncherVersion").getAsInt());
        newRoot.addProperty("type", "release");
        newRoot.addProperty("processArguments", "username_session_version");
        newRoot.addProperty("time", format.format(new Date()));
        newRoot.addProperty("releaseTime", format.format(new Date()));
        newRoot.addProperty("sync", false);
        newRoot.addProperty("minecraftArguments", argsBuilder.toString());
        newRoot.add("libraries", newArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String profileContent = gson.toJson(newRoot);

        String host = ext.getDeployHost();
        String username = ext.getDeployUsername();
        String password = ext.getDeployPassword();

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;
        String[] folders = ext.getRemoteProfileDir().split("/");
        for(String folder : folders){
            if(folder.length() > 0){
                try{
                    sftp.cd(folder);
                }catch(SftpException e){
                    sftp.mkdir(folder);
                    sftp.cd(folder);
                }
            }
        }
        sftp.put(new StringInputStream(profileContent), "launcherProfile.json");
        sftp.exit();
        session.disconnect();
    }
}
