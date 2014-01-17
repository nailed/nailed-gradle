package jk_5.nailed.gradle.tasks;

import com.google.common.base.Strings;
import com.jcraft.jsch.*;
import groovy.lang.Closure;
import jk_5.nailed.gradle.Constants;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.extension.NailedExtension;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.filters.StringInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * No description given
 *
 * @author jk-5
 */
public class UploadJarTask extends DefaultTask {

    @Getter @Setter private DelayedFile uploadFile;
    @Getter @Setter private String remoteDir;
    @Getter @Setter private String remoteFile;

    public UploadJarTask() {
        super();

        this.onlyIf(new Closure<Boolean>(this, this){
            @Override
            public Boolean call(Object... objects) {
                NailedExtension ext = NailedExtension.getInstance(UploadJarTask.this.getProject());
                String host = ext.getDeployHost();
                String username = ext.getDeployUsername();
                String password = ext.getDeployPassword();
                return !(Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password));
            }
        });
    }

    @TaskAction
    public void doTask() throws IOException, JSchException, SftpException{
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
        String[] folders = this.remoteDir.split("/");
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
        sftp.put(new FileInputStream(this.getUploadFile().call()), this.remoteFile);
        sftp.put(new StringInputStream(checksum), this.remoteFile + ".sha1");
        sftp.exit();
        session.disconnect();
    }
}
