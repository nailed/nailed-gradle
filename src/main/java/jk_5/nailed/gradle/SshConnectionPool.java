package jk_5.nailed.gradle;

import com.jcraft.jsch.*;
import jk_5.nailed.gradle.deploy.CredentialsExtension;
import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
public class SshConnectionPool {

    private static JSch jsch = new JSch();
    private static Session session;
    private static ChannelSftp sftp;

    public static ChannelSftp getConnection(Project project) throws JSchException{
        if(sftp != null){
            return sftp;
        }
        CredentialsExtension ext = CredentialsExtension.getInstance(project);
        session = jsch.getSession(ext.getDeployUsername(), ext.getDeployServer());
        session.setPassword(ext.getDeployPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        sftp = (ChannelSftp) channel;
        return sftp;
    }

    public static void close(){
        if(sftp != null && !sftp.isClosed()){
            sftp.exit();
            session.disconnect();
            sftp = null;
        }else if(sftp != null && sftp.isClosed()){
            sftp = null;
        }
    }

    public static void cleanup() throws SftpException{
        sftp.cd(sftp.getHome());
    }
}
