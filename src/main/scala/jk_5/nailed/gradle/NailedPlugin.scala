package jk_5.nailed.gradle

import jk_5.nailed.gradle.common.{SshConnectionPool, BasePlugin}
import scala.util.Properties
import jk_5.nailed.gradle.tasks._
import jk_5.nailed.gradle.extension.NailedExtension
import scala.collection.JavaConversions._
import jk_5.nailed.gradle.json.RestartLevel
import org.gradle.{BuildResult, BuildListener}
import org.gradle.api.invocation.Gradle
import org.gradle.api.initialization.Settings
import com.google.common.collect.ImmutableSet

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

  var updateLibraryListTask: UpdateRemoteLibraryList = _

  override def applyPlugin(){
    this.getProject.getGradle.addBuildListener(new BuildListener {
      override def projectsLoaded(gradle: Gradle){}
      override def projectsEvaluated(gradle: Gradle){}
      override def settingsEvaluated(settings: Settings){}
      override def buildStarted(gradle: Gradle){}
      override def buildFinished(result: BuildResult){
        SshConnectionPool.close()
      }
    })

    this.getProject.getExtensions.create(Constants.NAILED_EXTENSION, classOf[NailedExtension], this.getProject)

    val loadLibraryListTask = this.makeTask("loadLibraryList", classOf[LoadRemoteLibraryListTask])
    this.updateLibraryListTask = this.makeTask("updateLibraryList", classOf[UpdateRemoteLibraryList])
    this.updateLibraryListTask.dependsOn("loadLibraryList")
    loadLibraryListTask.setUpdateTask(this.updateLibraryListTask)

    val launcherProfileTask = this.makeTask("createLauncherProfile", classOf[CreateLauncherProfileTask])
    launcherProfileTask.setDestination(Constants.PROFILE_LOCATION)
    launcherProfileTask.setFmlJson(Constants.FML_JSON_URL)
    val uploadProfileTask = this.makeTask("deployLauncherProfile", classOf[UploadTask])
    uploadProfileTask.setRemoteDir("{REMOTE_DATA_DIR}")
    uploadProfileTask.setRemoteFile("launcherProfile.json")
    uploadProfileTask.setUploadFile(Constants.PROFILE_LOCATION)
    uploadProfileTask.setDestination("{MC_VERSION_DIR}/{MC_VERSION_NAME}.json")
    uploadProfileTask.setArtifact("launcherProfile")
    uploadProfileTask.setRestart(RestartLevel.LAUNCHER)
    uploadProfileTask.dependsOn("createLauncherProfile")
    uploadProfileTask.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
    uploadProfileTask.setUpdateTask(this.updateLibraryListTask)
  }

  override def afterEvaluate(){
    val ext = NailedExtension.getInstance(this.getProject)
    ext.getDeployed.foreach(p => {
      val task = this.makeTask("deploy" + p.getName, classOf[DeploySubprojectTask])
      task.setSubProject(p)
      task.dependsOn(p.getName + ":build")
      task.setDestination("{MC_LIB_DIR}/{ART_GROUP}/Nailed-{ART_NAME}/{ART_VERSION}/Nailed-{ART_NAME}-{ART_VERSION}.jar")
      task.setRestart(RestartLevel.GAME)
      task.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
      task.setUpdateTask(this.updateLibraryListTask)
    })
    ext.getDeployedMods.foreach(p => {
      val task = this.makeTask("deploy" + p.getName, classOf[DeploySubprojectTask])
      task.setSubProject(p)
      task.dependsOn("build")
      task.setDestination("{MC_LIB_DIR}/{ART_GROUP}/Nailed-{ART_NAME}/{ART_VERSION}/Nailed-{ART_NAME}-{ART_VERSION}.jar")
      task.setIsMod(isMod = true)
      task.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
      task.setUpdateTask(this.updateLibraryListTask)
    })
    val updateForgeTask = this.makeTask("updateForge", classOf[UpdateAdditionalLibraryTask])
    updateForgeTask.setDestination("{MC_LIB_DIR}/net/minecraftforge/forge/{MC_VERSION}-{FORGE_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}.jar")
    updateForgeTask.setLocation("http://files.minecraftforge.net/maven/net/minecraftforge/forge/{MC_VERSION}-{FORGE_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}-universal.jar")
    updateForgeTask.setArtifact("forge")
    updateForgeTask.setRestart(RestartLevel.NOTHING)
    updateForgeTask.dependsOn("deployLauncherProfile")
    updateForgeTask.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
    updateForgeTask.setUpdateTask(this.updateLibraryListTask)

    val updateMCTask = this.makeTask("updateMinecraft", classOf[UpdateAdditionalLibraryTask])
    updateMCTask.setDestination("{MC_LIB_DIR}/net/minecraft/minecraft/{MC_VERSION}/minecraft-{MC_VERSION}.jar")
    updateMCTask.setLocation(Constants.MINECRAFT_URL)
    updateMCTask.setArtifact("minecraft")
    updateMCTask.setRestart(RestartLevel.NOTHING)
    updateMCTask.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
    updateMCTask.setUpdateTask(this.updateLibraryListTask)
  }
}
