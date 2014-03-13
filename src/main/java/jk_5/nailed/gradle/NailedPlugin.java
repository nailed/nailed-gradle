package jk_5.nailed.gradle;

import jk_5.nailed.gradle.common.BasePlugin;
import jk_5.nailed.gradle.delayed.DelayedBase;
import jk_5.nailed.gradle.extension.NailedExtension;
import jk_5.nailed.gradle.tasks.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
public class NailedPlugin extends BasePlugin implements DelayedBase.IDelayedResolver {

    private CreateLauncherProfileTask launcherProfileTask;

    @Override
    public void applyPlugin() {
        this.getProject().getExtensions().create(Constants.NAILED_EXTENSION, NailedExtension.class, this.getProject());

        this.registerTasks();
    }

    public void registerTasks(){
        launcherProfileTask = this.makeTask("createLauncherProfile", CreateLauncherProfileTask.class);
        launcherProfileTask.setDestination(this.delayedFile(Constants.PROFILE_LOCATION));
        launcherProfileTask.setFmlJson(this.delayedString(Constants.FML_JSON_URL));

        UploadTask uploadProfileTask = this.makeTask("deployLauncherProfile", UploadTask.class);
        uploadProfileTask.setRemoteDir(this.delayedString("{REMOTE_DATA_DIR}"));
        uploadProfileTask.setRemoteFile(this.delayedString("launcherProfile.json"));
        uploadProfileTask.setUploadFile(this.delayedFile(Constants.PROFILE_LOCATION));
        uploadProfileTask.setDestination(this.delayedString("{MC_VERSION_DIR}/{MC_VERSION_NAME}.json"));
        uploadProfileTask.setArtifact(this.delayedString("launcherProfile"));
        uploadProfileTask.setRestart("launcher");
        uploadProfileTask.dependsOn("createLauncherProfile");

        DownloadTask downloadMinecraftTask = this.makeTask("downloadMinecraft", DownloadTask.class);
        downloadMinecraftTask.setUrl(this.delayedString(Constants.MINECRAFT_URL));
        downloadMinecraftTask.setOutput(this.delayedFile(Constants.MINECRAFT_CACHE));

        UploadTask deployMinecraftTask = this.makeTask("deployMinecraft", UploadTask.class);
        deployMinecraftTask.setRemoteDir(this.delayedString("jk_5/nailed/deploy/minecraft/{MC_VERSION}"));
        deployMinecraftTask.setRemoteFile(this.delayedString("minecraft-{MC_VERSION}.jar"));
        deployMinecraftTask.setUploadFile(this.delayedFile(Constants.MINECRAFT_CACHE));
        deployMinecraftTask.setDestination(this.delayedString("{MC_LIB_DIR}/jk_5/nailed/deploy/minecraft/{MC_VERSION}/minecraft-{MC_VERSION}.jar"));
        deployMinecraftTask.setArtifact(this.delayedString("minecraft"));
        deployMinecraftTask.setRestart("game");
        deployMinecraftTask.dependsOn("downloadMinecraft");
        launcherProfileTask.addDependency(this.delayedString("jk_5.nailed.deploy:minecraft:{MC_VERSION}"));

        DownloadTask downloadForgeTask = this.makeTask("downloadForge", DownloadTask.class);
        downloadForgeTask.setUrl(this.delayedString(Constants.FORGE_URL));
        downloadForgeTask.setOutput(this.delayedFile(Constants.FORGE_CACHE));

        UploadTask deployForgeTask = this.makeTask("deployForge", UploadTask.class);
        deployForgeTask.setRemoteDir(this.delayedString("jk_5/nailed/deploy/forge/{FORGE_VERSION}"));
        deployForgeTask.setRemoteFile(this.delayedString("forge-{FORGE_VERSION}.jar"));
        deployForgeTask.setUploadFile(this.delayedFile(Constants.FORGE_CACHE));
        deployForgeTask.setDestination(this.delayedString("{MC_LIB_DIR}/jk_5/nailed/deploy/forge/{FORGE_VERSION}/forge-{FORGE_VERSION}.jar"));
        deployForgeTask.setArtifact(this.delayedString("forge"));
        deployForgeTask.setRestart("game");
        deployForgeTask.dependsOn("downloadForge");
        launcherProfileTask.addDependency(this.delayedString("jk_5.nailed.deploy:forge:{FORGE_VERSION}"));

        this.makeTask("deploy", DefaultTask.class).dependsOn("deployLauncherProfile", "deployMinecraft", "deployForge");
    }

    @Override
    public void afterEvaluate(){
        super.afterEvaluate();

        for(Project p : NailedExtension.getInstance(this.getProject()).getDeployedProjects()){
            DeploySubprojectTask task = this.makeTask("deploy" + p.getName(), DeploySubprojectTask.class);
            task.setSubProject(p);
            task.dependsOn("build");
            this.getProject().getTasks().getByName("deploy").dependsOn("deploy" + p.getName());
        }

        for(String l : NailedExtension.getInstance(this.getProject()).getAdditionalLibs()){
            UpdateAdditionalLibraryTask task = this.makeTask("update" + l.split(":")[1], UpdateAdditionalLibraryTask.class);
            task.setMavenPath(this.delayedString(l));
            //task.setDestination(this.delayedString());
            this.launcherProfileTask.addDependency(this.delayedString(l));
        }
    }

    @Override
    public String resolve(String pattern, Project project) {
        return pattern;
    }
}
