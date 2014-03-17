package jk_5.nailed.gradle.tasks;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.*;
import groovy.lang.Closure;
import jk_5.nailed.gradle.Constants;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.delayed.DelayedString;
import jk_5.nailed.gradle.extension.NailedExtension;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.filters.StringInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * No description given
 *
 * @author jk-5
 */
public class UploadTask extends DefaultTask {

    @Getter @Setter private DelayedFile uploadFile;
    @Getter @Setter private DelayedString remoteDir;
    @Getter @Setter private DelayedString remoteFile;
    @Getter @Setter private DelayedString destination;
    @Getter @Setter private DelayedString artifact;
    @Getter @Setter private String restart = "nothing";
    @Getter @Setter private boolean mod = false;

    public UploadTask() {
        super();

        this.onlyIf(new Closure<Boolean>(this, this){
            @Override
            public Boolean call(Object... objects) {
                NailedExtension ext = NailedExtension.getInstance(UploadTask.this.getProject());
                String host = ext.getDeployHost();
                String username = ext.getDeployUsername();
                String password = ext.getDeployPassword();
                return !(Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password));
            }
        });
    }

    @TaskAction
    public void doTask() throws IOException, JSchException, SftpException, InterruptedException{
        NailedExtension ext = NailedExtension.getInstance(this.getProject());

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
        String[] folders = this.remoteDir.call().split("/");
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
        String checksum = Constants.getSHA1(this.getUploadFile().call());
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(sftp.get(this.remoteFile.call() + ".sha1")));
            String remote = reader.readLine();
            reader.close();
            if(checksum.equals(remote) && System.getProperty("forceUpdate", "false").equals("false")){
                //Checksums match, don't update
                this.getLogger().lifecycle("Local checksum matched remote checksum. Not updating!");
                sftp.exit();
                channel.disconnect();
                return;
            }
        }catch(Exception e){
            //Go ahead, remote checksum not found
        }

        sftp.put(new FileInputStream(this.getUploadFile().call()), this.remoteFile.call());
        sftp.put(new StringInputStream(checksum), this.remoteFile.call() + ".sha1");

        sftp.cd(sftp.getHome());
        sftp.cd(ext.getRemoteProfileDir());
        JsonObject versionData;
        try{
            versionData = new JsonParser().parse(new InputStreamReader(sftp.get("versions-1.json"))).getAsJsonObject();
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
        if(fileInfo.has("mod")){
            fileInfo.remove("mod");
        }
        fileInfo.addProperty("destination", this.destination.call());
        fileInfo.addProperty("location", ext.getLoadingMavenUrl() + this.remoteDir.call() + "/" + this.remoteFile.call());
        if(!this.restart.equals("nothing")) fileInfo.addProperty("restart", this.restart);
        fileInfo.addProperty("mod", this.mod);
        sftp.put(new StringInputStream(new Gson().toJson(versionData)), "versions-1.json");
        sftp.exit();
        session.disconnect();
    }
}
