package jk_5.nailed.gradle;

import jk_5.nailed.gradle.common.BasePlugin;
import jk_5.nailed.gradle.delayed.DelayedBase;
import jk_5.nailed.gradle.extension.NailedExtension;
import jk_5.nailed.gradle.tasks.DeployLauncherProfileTask;
import jk_5.nailed.gradle.tasks.DownloadTask;
import jk_5.nailed.gradle.tasks.UploadJarTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
public class NailedPlugin extends BasePlugin implements DelayedBase.IDelayedResolver {

    private boolean jsonApplied = false;

    @Override
    public void applyPlugin() {

        //this.applyExternalPlugin("forge");
        //this.applyExternalPlugin("maven");

        this.getProject().getExtensions().create(Constants.NAILED_EXTENSION, NailedExtension.class, this.getProject());

        this.registerLauncherTasks();
    }

    public void registerLauncherTasks(){
        DeployLauncherProfileTask task = this.makeTask("deployLauncherProfile", DeployLauncherProfileTask.class);

        DownloadTask downloadMinecraftTask = this.makeTask("downloadMinecraft", DownloadTask.class);
        downloadMinecraftTask.setUrl(this.delayedString(Constants.MINECRAFT_URL));
        downloadMinecraftTask.setOutput(this.delayedFile(Constants.MINECRAFT_CACHE));

        UploadJarTask deployMinecraftTask = this.makeTask("deployMinecraft", UploadJarTask.class);
        deployMinecraftTask.setRemoteDir("jk_5/nailed/deploy/minecraft/1.0");
        deployMinecraftTask.setRemoteFile("minecraft-1.0.jar");
        deployMinecraftTask.setUploadFile(this.delayedFile(Constants.MINECRAFT_CACHE));
        deployMinecraftTask.dependsOn("downloadMinecraft");

        DownloadTask downloadForgeTask = this.makeTask("downloadForge", DownloadTask.class);
        downloadForgeTask.setUrl(this.delayedString(Constants.FORGE_URL));
        downloadForgeTask.setOutput(this.delayedFile(Constants.FORGE_CACHE));

        UploadJarTask deployForgeTask = this.makeTask("deployForge", UploadJarTask.class);
        deployForgeTask.setRemoteDir("jk_5/nailed/deploy/forge/1.0");
        deployForgeTask.setRemoteFile("forge-1.0.jar");
        deployForgeTask.setUploadFile(this.delayedFile(Constants.FORGE_CACHE));
        deployForgeTask.dependsOn("downloadForge");

        this.makeTask("deploy", DefaultTask.class).dependsOn("deployLauncherProfile", "deployMinecraft", "deployForge");
    }

    @Override
    public String resolve(String pattern, Project project) {
        return pattern;
    }
}
