package jk_5.nailed.gradle.tasks.deploy;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import groovy.lang.Closure;
import jk_5.nailed.gradle.SshConnectionPool;
import jk_5.nailed.gradle.common.StringOutputStream;
import jk_5.nailed.gradle.deploy.CredentialsExtension;
import jk_5.nailed.gradle.deploy.DeployExtension;
import jk_5.nailed.gradle.deploy.DeployTask;
import jk_5.nailed.gradle.json.deploy.Library;
import jk_5.nailed.gradle.json.deploy.LibraryList;
import jk_5.nailed.gradle.json.deploy.LibraryListSerializer;
import org.apache.tools.ant.filters.StringInputStream;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class UpdateRemoteLibraryListTask extends DeployTask {

    private final List<Library> librariesToUpdate = Lists.newArrayList();
    private static final Joiner folderJoiner = Joiner.on("/").skipNulls();

    public UpdateRemoteLibraryListTask(){
        super();

        this.onlyIf(new Closure<Boolean>(this, this) {
            @Override
            public Boolean call(Object... args){
                return !UpdateRemoteLibraryListTask.this.librariesToUpdate.isEmpty();
            }
        });
    }

    @TaskAction
    public void doTask() throws JSchException, SftpException, IOException, InterruptedException{
        CredentialsExtension ext = CredentialsExtension.getInstance(this.getProject());
        DeployExtension ext1 = DeployExtension.getInstance(this.getProject());
        ChannelSftp sftp = SshConnectionPool.getConnection(this.getProject());
        gotoDir(sftp, ext.getVersionFile());
        while(isLocked(sftp, ext.getVersionFile())){
            Thread.sleep(1000);
        }
        lock(sftp, ext.getVersionFile());
        try{
            StringOutputStream output = new StringOutputStream();
            sftp.get(getFileWithoutPath(ext.getVersionFile()), output);
            LibraryList list = LibraryListSerializer.serializer.fromJson(output.toString(), LibraryList.class);
            list.addLibs(this.librariesToUpdate, ext1.isVersionUpdates());
            sftp.put(new StringInputStream(LibraryListSerializer.serializer.toJson(list)), getFileWithoutPath(ext.getVersionFile()));
        }finally{
            unlock(sftp, ext.getVersionFile());
        }
        SshConnectionPool.cleanup();
    }

    public void addLibrary(Library library){
        this.librariesToUpdate.add(library);
    }

    private static String getFileDir(String file){
        String[] folders = file.split("/");
        folders[folders.length - 1] = null;
        return folderJoiner.join(folders);
    }

    private static String getFileWithoutPath(String path){
        String[] folders = path.split("/");
        return folders[folders.length - 1];
    }

    private static boolean isLocked(ChannelSftp sftp, String file) throws SftpException, IOException{
        try{
            sftp.get(getFileWithoutPath(file + ".lock")).close();
            return true;
        }catch(SftpException e){
            //File does not exist, we don't have a lock
            return false;
        }
    }

    private static void lock(ChannelSftp sftp, String file) throws SftpException, IOException{
        sftp.put(new StringInputStream(""), getFileWithoutPath(file) + ".lock");
    }

    private static void unlock(ChannelSftp sftp, String file) throws SftpException, IOException{
        sftp.rm(getFileWithoutPath(file) + ".lock");
    }

    private static void gotoDir(ChannelSftp channel, String folder) throws SftpException{
        String[] folders = getFileDir(folder).split("/");
        for(String f : folders){
            if(f.length() > 0){
                try{
                    channel.cd(f);
                }catch(SftpException e){
                    channel.mkdir(f);
                    channel.cd(f);
                }
            }
        }
    }
}
