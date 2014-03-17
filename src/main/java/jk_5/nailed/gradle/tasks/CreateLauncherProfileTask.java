package jk_5.nailed.gradle.tasks;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import jk_5.nailed.gradle.Constants;
import jk_5.nailed.gradle.common.Pair;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.delayed.DelayedString;
import jk_5.nailed.gradle.extension.NailedExtension;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class CreateLauncherProfileTask extends DefaultTask {

    @Getter @Setter private DelayedString fmlJson;
    @Getter @Setter private DelayedFile destination;
    private final List<Pair<DelayedString, DelayedString>> dependencies = Lists.newArrayList();

    @TaskAction
    public void doTask() throws IOException, JSchException, SftpException {
        NailedExtension ext = NailedExtension.getInstance(getProject());
        URL url = new URL(new DelayedString(this.getProject(), Constants.FML_JSON_URL).call());
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        Reader reader = new InputStreamReader(conn.getInputStream());
        JsonElement parsed = new JsonParser().parse(reader);
        JsonObject remoteInfo = parsed.getAsJsonObject().getAsJsonObject("versionInfo");
        JsonArray libs = remoteInfo.getAsJsonArray("libraries");
        JsonArray newArray = new JsonArray();
        for(Project project : ext.getDeployed()){
            this.addDependency(new DelayedString(this.getProject(), project.getGroup() + ":Nailed-" + project.getName() + ":" + project.getVersion()));
        }
        /*for(Project project : ext.getDeployedMods()){
            this.addDependency(new DelayedString(this.getProject(), project.getGroup() + ":Nailed-" + project.getName() + ":" + project.getVersion()));
        }*/
        for(Pair<DelayedString, DelayedString> dep : this.dependencies){
            JsonObject obj = new JsonObject();
            obj.addProperty("name", dep.getKey().call());
            if(!dep.getValue().call().equals(Constants.MINECRAFT_MAVEN)) obj.addProperty("url", dep.getValue().call());
            newArray.add(obj);
        }
        for(JsonElement element : libs){
            JsonObject object = element.getAsJsonObject();
            if(!object.get("name").getAsString().equals("@artifact@")){
                JsonObject newObject = new JsonObject();
                String name = object.get("name").getAsString();
                if(name.startsWith("org.scala-lang")){ //Use our mirror for scala libs. Forge only hosts compressed versions
                    object.remove("url");
                    object.addProperty("url", "http://maven.reening.nl/");
                }
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
        StringBuilder argsBuilder = new StringBuilder("--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${game_assets} --uuid ${auth_uuid} --accessToken ${auth_access_token}");
        for(String string : ext.getTweakers()){
            argsBuilder.append(" --tweakClass ");
            argsBuilder.append(string);
        }

        JsonObject newRoot = new JsonObject();
        newRoot.addProperty("id", ext.getVersionName());
        newRoot.addProperty("mainClass", ext.getMainClass());
        newRoot.addProperty("minimumLauncherVersion", remoteInfo.get("minimumLauncherVersion").getAsInt());
        newRoot.addProperty("type", "release");
        newRoot.addProperty("time", format.format(new Date()));
        newRoot.addProperty("releaseTime", format.format(new Date()));
        newRoot.addProperty("sync", false);
        newRoot.addProperty("minecraftArguments", argsBuilder.toString());
        newRoot.add("libraries", newArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(this.destination.call().isFile()) this.destination.call().delete();
        FileWriter writer = new FileWriter(this.destination.call());
        gson.toJson(newRoot, writer);
        writer.close();
    }

    public void addDependency(DelayedString name){
        this.dependencies.add(new Pair<>(name, new DelayedString(this.getProject(), "{MAVEN_URL}")));
    }

    public void addDependency(DelayedString name, DelayedString mavenUrl){
        this.dependencies.add(new Pair<>(name, mavenUrl));
    }
}
