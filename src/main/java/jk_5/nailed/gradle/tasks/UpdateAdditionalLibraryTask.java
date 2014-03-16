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

/**
 * No description given
 *
 * @author jk-5
 */
public class UpdateAdditionalLibraryTask extends DefaultTask {

    @Getter @Setter private DelayedString destination;
    @Getter @Setter private DelayedString location;
    @Getter @Setter private String artifact;
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

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        sftp.cd(ext.getRemoteProfileDir());
        JsonObject versionData;
        try{
            versionData = new JsonParser().parse(new InputStreamReader(sftp.get("versions-1.json"))).getAsJsonObject();
        }catch(Exception e){
            versionData = new JsonObject();
        }
        if(!versionData.has(artifact)){
            versionData.add(artifact, new JsonObject());
        }
        JsonObject fileInfo = versionData.getAsJsonObject(artifact);
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
        fileInfo.addProperty("location", this.location.call());
        if(!this.restart.equals("nothing")) fileInfo.addProperty("restart", this.restart);
        sftp.put(new StringInputStream(new Gson().toJson(versionData)), "versions-1.json");
        sftp.exit();
        session.disconnect();
    }
}
