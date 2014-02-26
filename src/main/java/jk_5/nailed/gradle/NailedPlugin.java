package jk_5.nailed.gradle;

import com.google.common.base.Throwables;
import jk_5.nailed.gradle.common.BasePlugin;
import jk_5.nailed.gradle.delayed.DelayedBase;
import jk_5.nailed.gradle.extension.NailedExtension;
import jk_5.nailed.gradle.json.JsonFactory;
import jk_5.nailed.gradle.json.dependencies.DependencyFile;
import jk_5.nailed.gradle.json.dependencies.Library;
import jk_5.nailed.gradle.tasks.CreateLauncherProfileTask;
import jk_5.nailed.gradle.tasks.CreateRemoteDepFileTask;
import jk_5.nailed.gradle.tasks.UploadTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.File;

/**
 * No description given
 *
 * @author jk-5
 */
public class NailedPlugin extends BasePlugin implements DelayedBase.IDelayedResolver {

    private boolean jsonApplied = false;
    private DependencyFile depFile = null;

    @Override
    public void applyPlugin() {
        this.getProject().getExtensions().create(Constants.NAILED_EXTENSION, NailedExtension.class, this.getProject());

        this.configureDeps();
        this.registerTasks();
    }

    public void registerTasks(){
        /*UploadTask uploadProfileTask = this.makeTask("deployLauncherProfile", UploadTask.class);
        uploadProfileTask.setRemoteDir(this.delayedString("{REMOTE_DATA_DIR}"));
        uploadProfileTask.setRemoteFile(this.delayedString("launcherProfile.json"));
        uploadProfileTask.setUploadFile(this.delayedFile(Constants.PROFILE_LOCATION));
        uploadProfileTask.setDestination(this.delayedString("{MC_VERSION_DIR}/{MC_VERSION_NAME}.json"));
        uploadProfileTask.setArtifact(this.delayedString("launcherProfile"));
        uploadProfileTask.setRestart("launcher");
        uploadProfileTask.dependsOn("createLauncherProfile");*/

        /*DownloadTask downloadMinecraftTask = this.makeTask("downloadMinecraft", DownloadTask.class);
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

        this.makeTask("deploy", DefaultTask.class).dependsOn("deployLauncherProfile", "deployMinecraft", "deployForge");*/
    }

    public void configureDeps(){
        this.getProject().getConfigurations().create(Constants.CONFIG_DEPS);
    }

    @Override
    public void afterEvaluate(){
        super.afterEvaluate();

        this.getProject().getDependencies().add("compile", this.getProject().getConfigurations().getByName(Constants.CONFIG_DEPS));

        if(this.delayedFile(Constants.JSON_LOCATION).call().exists()){
            this.readAndApplyJson(this.delayedFile(Constants.JSON_LOCATION).call(), Constants.CONFIG_DEPS);
        }

        CreateLauncherProfileTask launcherProfileTask = this.makeTask("createLauncherProfile", CreateLauncherProfileTask.class);
        launcherProfileTask.setDestination(this.delayedFile(Constants.PROFILE_LOCATION));
        launcherProfileTask.setDependencyFile(this.depFile);

        CreateRemoteDepFileTask remoteDepFileTask = this.makeTask("createDepFile", CreateRemoteDepFileTask.class);
        remoteDepFileTask.setDestination(this.delayedFile(Constants.DEPFILE_LOCATION));
        remoteDepFileTask.setDependencyFile(this.depFile);

        UploadTask uploadDepFile = this.makeTask("uploadDepFile", UploadTask.class);
        uploadDepFile.setArtifact(this.delayedString("depFile"));
        uploadDepFile.setRemoteDir(this.delayedString("nailed"));
        uploadDepFile.setRemoteFile(this.delayedString("dependencies.json"));
        uploadDepFile.setUploadFile(this.delayedFile(Constants.DEPFILE_LOCATION));
        uploadDepFile.dependsOn("createDepFile");

        /*for(Project p : NailedExtension.getInstance(this.getProject()).getDeployed()){
            DeploySubprojectTask task = this.makeTask("deploy" + p.getName(), DeploySubprojectTask.class);
            task.setSubProject(p);
            task.setDestLocation("{MC_LIB_DIR}/{GROUP}/Nailed-{NAME}/{VERSION}/Nailed-{NAME}-{VERSION}.jar");
            task.setRestart("game");
            //task.dependsOn(":" + p.getName() + ":assemble", "reobf");
            task.dependsOn("build");
            this.getProject().getTasks().getByName("deploy").dependsOn("deploy" + p.getName());
        }
        for(Project p : NailedExtension.getInstance(this.getProject()).getDeployedMods()){
            DeploySubprojectTask task = this.makeTask("deploy" + p.getName(), DeploySubprojectTask.class);
            task.setSubProject(p);
            task.setDestLocation("{MC_GAME_DIR}/mods/Nailed-{NAME}-{VERSION}.jar");
            //task.dependsOn(":" + p.getName() + ":assemble", "reobf");
            task.dependsOn("build");
            this.getProject().getTasks().getByName("deploy").dependsOn("deploy" + p.getName());
        }*/
    }

    private void readAndApplyJson(File file, String configDeps){
        if(this.depFile == null){
            try{
                this.depFile = JsonFactory.loadDependencyFile(file);
            }catch(Exception e){
                this.getProject().getLogger().error(file + " could not be parsed");
                Throwables.propagate(e);
            }
        }

        if(this.jsonApplied) return;

        DependencyHandler depHandler = this.getProject().getDependencies();

        if(this.getProject().getConfigurations().getByName(configDeps).getState() == Configuration.State.UNRESOLVED){
            for(Library lib : this.depFile.libraries){
                if(lib.dev){
                    this.getProject().getLogger().lifecycle("Adding " + lib.getArtifactName());
                    depHandler.add(configDeps, lib.getArtifactName());
                }else{
                    this.getProject().getLogger().lifecycle("Resolved " + lib.getArtifactName());
                }
            }
        }
    }

    @Override
    public String resolve(String pattern, Project project) {
        return pattern;
    }
}
