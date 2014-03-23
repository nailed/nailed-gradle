package jk_5.nailed.gradle

import jk_5.nailed.gradle.common.BasePlugin
import scala.util.Properties
import jk_5.nailed.gradle.tasks.{UpdateAdditionalLibraryTask, DeploySubprojectTask, UploadTask, CreateLauncherProfileTask}
import jk_5.nailed.gradle.extension.NailedExtension
import org.gradle.api.DefaultTask
import scala.collection.JavaConversions._
import jk_5.nailed.gradle.json.{RestartLevel, LauncherLibrary}

/**
 * No description given
 *
 * @author jk-5
 */
object Constants {
  final val NEWLINE = Properties.propOrElse("line.separator", "\n")
  final val NAILED_EXTENSION = "nailed"
  final val NAILED_JSON = "{JSON_FILE}"
  final val DEPENDENCY_CONFIG = "nailed"
  final val FML_JSON_URL = "https://raw.github.com/MinecraftForge/FML/master/jsons/{MC_VERSION}-rel.json"
  final val MINECRAFT_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/{MC_VERSION}/{MC_VERSION}.jar"
  final val MINECRAFT_CACHE = "{CACHE_DIR}/{MC_VERSION}/minecraft-{MC_VERSION}.jar"
  final val FORGE_URL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/{MC_VERSION}-{FORGE_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}-universal.jar"
  final val FORGE_CACHE = "{CACHE_DIR}/{MC_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}.jar"
  final val CLIENT_LOCATION = "{BUILD_DIR}/libs/Nailed-Client-{CLIENT_VERSION}.jar"
  final val PROFILE_LOCATION = "{CACHE_DIR}/{MC_VERSION}/launcherProfile.json"
  final val MINECRAFT_MAVEN = "https://libraries.mojang.com/"
  final val REMOTE_VERSION_FILE = "versions-2.json"
}

class NailedPlugin extends BasePlugin {
  private var launcherProfileTask: CreateLauncherProfileTask = null

  override def applyPlugin(){
    this.getProject.getExtensions.create(Constants.NAILED_EXTENSION, classOf[NailedExtension], this.getProject)
    this.registerTasks()
  }

  def registerTasks(){
    launcherProfileTask = this.makeTask("createLauncherProfile", classOf[CreateLauncherProfileTask])
    launcherProfileTask.setDestination(this.delayedFile(Constants.PROFILE_LOCATION))
    launcherProfileTask.setFmlJson(this.delayedString(Constants.FML_JSON_URL))
    val uploadProfileTask = this.makeTask("deployLauncherProfile", classOf[UploadTask])
    uploadProfileTask.setRemoteDir(this.delayedString("{REMOTE_DATA_DIR}"))
    uploadProfileTask.setRemoteFile(this.delayedString("launcherProfile.json"))
    uploadProfileTask.setUploadFile(this.delayedFile(Constants.PROFILE_LOCATION))
    uploadProfileTask.setDestination(this.delayedString("{MC_VERSION_DIR}/{MC_VERSION_NAME}.json"))
    uploadProfileTask.setArtifact(this.delayedString("launcherProfile"))
    uploadProfileTask.setRestart(RestartLevel.LAUNCHER)
    uploadProfileTask.dependsOn("createLauncherProfile")
    this.makeTask("deploy", classOf[DefaultTask]).dependsOn("deployLauncherProfile")
  }

  override def afterEvaluate(){
    val ext = NailedExtension.getInstance(this.getProject)
    ext.getDeployed.foreach(p => {
      val task = this.makeTask("deploy" + p.getName, classOf[DeploySubprojectTask])
      task.setSubProject(p)
      task.dependsOn(p.getName + ":build")
      task.setDestination(this.delayedString("{MC_LIB_DIR}/{ART_GROUP}/Nailed-{ART_NAME}/{ART_VERSION}/Nailed-{ART_NAME}-{ART_VERSION}.jar"))
      this.getProject.getTasks.getByName("deploy").dependsOn("deploy" + p.getName)
      task.setRestart(RestartLevel.GAME)
    })
    ext.getDeployedMods.foreach(p => {
      val task = this.makeTask("deploy" + p.getName, classOf[DeploySubprojectTask])
      task.setSubProject(p)
      task.dependsOn("build")
      task.setDestination(this.delayedString("{MC_LIB_DIR}/{ART_GROUP}/Nailed-{ART_NAME}/{ART_VERSION}/Nailed-{ART_NAME}-{ART_VERSION}.jar"))
      task.setIsMod(true)
      this.getProject.getTasks.getByName("deploy").dependsOn("deploy" + p.getName)
    })
    val updateForgeTask = this.makeTask("updateForge", classOf[UpdateAdditionalLibraryTask])
    updateForgeTask.setDestination(this.delayedString("{MC_LIB_DIR}/net/minecraftforge/forge/{MC_VERSION}-{FORGE_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}.jar"))
    updateForgeTask.setLocation(this.delayedString("http://files.minecraftforge.net/maven/net/minecraftforge/forge/{MC_VERSION}-{FORGE_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}-universal.jar"))
    updateForgeTask.setArtifact("forge")
    updateForgeTask.setRestart(RestartLevel.NOTHING)
    launcherProfileTask.addDependency(new LauncherLibrary("net.minecraftforge:forge:{MC_VERSION}-{FORGE_VERSION}"))

    val updateMCTask = this.makeTask("updateMinecraft", classOf[UpdateAdditionalLibraryTask])
    updateMCTask.setDestination(this.delayedString("{MC_LIB_DIR}/net/minecraft/minecraft/{MC_VERSION}/minecraft-{MC_VERSION}.jar"))
    updateMCTask.setLocation(this.delayedString(Constants.MINECRAFT_URL))
    updateMCTask.setArtifact("minecraft")
    updateMCTask.setRestart(RestartLevel.NOTHING)
    launcherProfileTask.addDependency(new LauncherLibrary("net.minecraft:minecraft:{MC_VERSION}"))
  }
}
