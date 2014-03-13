package jk_5.nailed.gradle.tasks;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.*;
import groovy.lang.Closure;
import jk_5.nailed.gradle.delayed.DelayedString;
import jk_5.nailed.gradle.extension.NailedExtension;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.filters.StringInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.InputStreamReader;
import java.util.Properties;

/**
 * No description given
 *
 * @author jk-5
 */
public class UpdateAdditionalLibraryTask extends DefaultTask {

    @Getter @Setter private DelayedString mavenPath;
    @Getter @Setter private DelayedString artifact;
    @Getter @Setter private DelayedString destination;
    @Getter @Setter private String restart;

    public UpdateAdditionalLibraryTask() {
        super();

        this.onlyIf(new Closure<Boolean>(this, this){
            @Override
            public Boolean call(Object... objects) {
                NailedExtension ext = NailedExtension.getInstance(UpdateAdditionalLibraryTask.this.getProject());
                String host = ext.getDeployHost();
                String username = ext.getDeployUsername();
                String password = ext.getDeployPassword();
                return !(Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password));
            }
        });
    }

    @TaskAction
    public void doTask() throws JSchException, SftpException{
        NailedExtension ext = NailedExtension.getInstance(this.getProject());

        String host = ext.getDeployHost();
        String username = ext.getDeployUsername();
        String password = ext.getDeployPassword();

        String group = this.mavenPath.call().split(":", 2)[0];
        String artifact = this.mavenPath.call().split(":", 2)[1];
        String version = this.mavenPath.call().split(":", 2)[2];
        String location = group.replace(".", "/") + "/" + artifact + "/" + artifact + "-" + version + ".jar";

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
        JsonObject versionData;
        try{
            versionData = new JsonParser().parse(new InputStreamReader(sftp.get("versions.json"))).getAsJsonObject();
        }catch(Exception e){
            versionData = new JsonObject();
        }
        if(!versionData.has(this.artifact.call())){
            versionData.add(this.artifact.call(), new JsonObject());
        }
        JsonObject fileInfo = versionData.getAsJsonObject(this.artifact.call());
        if(!fileInfo.has("rev")){
            fileInfo.addProperty("rev", 0);
        }else{
            int rev = fileInfo.get("rev").getAsInt() + 1;
            fileInfo.remove("rev");
            fileInfo.addProperty("rev", rev);
        }
        if(fileInfo.has("destination")){
            fileInfo.remove("destination");
        }
        if(fileInfo.has("location")){
            fileInfo.remove("location");
        }
        if(fileInfo.has("restart")){
            fileInfo.remove("restart");
        }
        fileInfo.addProperty("destination", this.destination.call());
        fileInfo.addProperty("location", location);
        if(!this.restart.equals("no")) fileInfo.addProperty("restart", this.restart);
        sftp.put(new StringInputStream(new Gson().toJson(versionData)), "versions.json");
        sftp.exit();
        session.disconnect();
    }
}
