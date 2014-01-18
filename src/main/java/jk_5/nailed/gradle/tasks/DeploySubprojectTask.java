package jk_5.nailed.gradle.tasks;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.delayed.DelayedString;
import lombok.Setter;
import org.gradle.api.Project;

import java.io.IOException;

/**
 * No description given
 *
 * @author jk-5
 */
public class DeploySubprojectTask extends UploadTask {

    @Setter
    private Project subProject;

    @Override
    public void doTask() throws IOException, JSchException, SftpException, InterruptedException{
        String name = this.subProject.getName();
        String version = this.subProject.getVersion().toString();
        String groupDir = this.subProject.getGroup().toString().replace('.', '/');

        this.setRemoteDir(this.delayedString(groupDir + "/Nailed-" + name + "/" + version));
        this.setRemoteFile(this.delayedString("Nailed-" + name + "-" + version + ".jar"));
        this.setUploadFile(this.delayedFile("{BUILD_DIR}/libs/Nailed-" + name + "-" + version + ".jar"));
        this.setDestination(this.delayedString("{MC_LIB_DIR}/" + groupDir + "/Nailed-" + name + "/" + version + "/Nailed-" + name + "-" + version + ".jar"));
        this.setArtifact(this.delayedString("nailed" + name));
        this.setRestart("game");

        super.doTask();
    }

    private DelayedString delayedString(String string){
        return new DelayedString(this.getProject(), string);
    }

    private DelayedFile delayedFile(String string){
        return new DelayedFile(this.getProject(), string);
    }
}
