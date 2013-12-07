package jk_5.nailed.gradle.tasks;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.jcraft.jsch.*;
import groovy.lang.Closure;
import jk_5.nailed.gradle.Constants;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.delayed.DelayedString;
import jk_5.nailed.gradle.extension.LauncherExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
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
                LauncherExtension ext = LauncherExtension.getInstance(DeployLauncherProfileTask.this.getProject());
                String host = ext.getDeployHost();
                String username = ext.getDeployUsername();
                String password = ext.getDeployPassword();
                return !(Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password));
            }
        });
    }

    @TaskAction
    public void doTask() throws IOException, JSchException, SftpException {
        LauncherExtension ext = LauncherExtension.getInstance(this.getProject());
        URL url = new URL(new DelayedString(this.getProject(), Constants.FML_JSON_URL).call());
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        Reader reader = new InputStreamReader(conn.getInputStream());
        JsonElement parsed = new JsonParser().parse(reader);
        JsonObject remoteInfo = parsed.getAsJsonObject().getAsJsonObject("versionInfo");
        JsonArray libs = remoteInfo.getAsJsonArray("libraries");
        JsonArray newArray = new JsonArray();
        for(JsonObject dep : ext.getLoadingDependencies()){
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
        StringBuilder argsBuilder = new StringBuilder("--username ${auth_player_name} --session ${auth_session} --version ${version_name} --gameDir ${game_directory} --assetsDir ${game_assets}");
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
        File tempFile = new DelayedFile(this.getProject(), "{CACHE_DIR}/profile.json").call();
        tempFile.getParentFile().mkdirs();
        if(tempFile.exists()) tempFile.delete();
        Writer writer = new PrintWriter(tempFile);
        writer.write(gson.toJson(newRoot));
        writer.close();

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
        sftp.cd(ext.getRemoteProfileDir());
        sftp.put(new FileInputStream(tempFile), "launcherProfile.json");
        sftp.exit();
        session.disconnect();
        tempFile.delete();
    }
}
