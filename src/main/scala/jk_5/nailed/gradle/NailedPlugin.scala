package jk_5.nailed.gradle

import jk_5.nailed.gradle.common.{MavenArtifact, DeployedArtifact, SshConnectionPool, BasePlugin}
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
  final val FML_JSON_URL = "https://raw.github.com/MinecraftForge/FML/master/jsons/{MC_VERSION}-rel.json"
  final val PROFILE_LOCATION = "{CACHE_DIR}/{MC_VERSION}/launcherProfile.json"
  final val MINECRAFT_MAVEN = "https://libraries.mojang.com/"
  final val REMOTE_VERSION_FILE = "versions-2.json"
}

class NailedPlugin extends BasePlugin {

  var updateLibraryListTask: UpdateRemoteLibraryListTask = _

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
    this.updateLibraryListTask = this.makeTask("updateLibraryList", classOf[UpdateRemoteLibraryListTask])
    this.updateLibraryListTask.dependsOn("loadLibraryList")
    loadLibraryListTask.setUpdateTask(this.updateLibraryListTask)

    val launcherProfileTask = this.makeTask("createLauncherProfile", classOf[CreateLauncherProfileTask])
    launcherProfileTask.setDestination(Constants.PROFILE_LOCATION)
    launcherProfileTask.setFmlJson(Constants.FML_JSON_URL)
    val uploadProfileTask = this.makeTask("uploadLauncherProfile", classOf[UploadTask])
    uploadProfileTask.setRemoteDir("{REMOTE_DATA_DIR}")
    uploadProfileTask.setRemoteFile("launcherProfile.json")
    uploadProfileTask.setUploadFile(Constants.PROFILE_LOCATION)
    uploadProfileTask.setDestination("{MC_VERSION_DIR}/{MC_VERSION_NAME}.json")
    uploadProfileTask.setArtifact("launcherProfile")
    uploadProfileTask.setRestart(RestartLevel.LAUNCHER)
    uploadProfileTask.dependsOn("createLauncherProfile")
    uploadProfileTask.setFinalizedBy(ImmutableSet.of("updateLauncherProfile"))
    val updateTask = this.makeTask("updateLauncherProfile", classOf[UpdateLibraryTask])
    updateTask.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
    updateTask.setUpdateTask(this.updateLibraryListTask)
    uploadProfileTask.setUpdateTask(updateTask)
    this.updateLibraryListTask.setLauncherProfileTask(launcherProfileTask)

    val notificationTask = this.makeTask("notification", classOf[UpdateNotificationTask])
    notificationTask.setUpdateTask(this.updateLibraryListTask)
    this.updateLibraryListTask.setFinalizedBy(ImmutableSet.of("notification"))
  }

  override def afterEvaluate(){
    val ext = NailedExtension.getInstance(this.getProject)
    for(pt <- ext.getDeployed) pt match {
      case p: DeployedArtifact =>
        val updateTask = this.makeTask("update" + p.projectName, classOf[UpdateLibraryTask])
        updateTask.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
        updateTask.setUpdateTask(this.updateLibraryListTask)
        val task = this.makeTask("upload" + p.projectName, classOf[UploadSubprojectTask])
        val proj = this.getProject.getSubprojects.find(_.getName == p.projectName).get
        task.setSubProject(proj)
        if(p.reobf){
          task.dependsOn("build")
        }else{
          task.dependsOn(p.projectName + ":build")
        }
        task.setArtifact(p.artifact)
        task.setRestart(p.restart)
        task.setIsMod(p.mod)
        task.setLoad(p.load)
        task.setCoremod(p.coremod)
        if(p.mclib){
          task.setDestination("{MC_LIB_DIR}/{ART_GROUP}/Nailed-{ART_NAME}/{ART_VERSION}/Nailed-{ART_NAME}-{ART_VERSION}.jar")
        }else{
          task.setDestination("{NAILED_LIB_DIR}/{ART_GROUP}/Nailed-{ART_NAME}/{ART_VERSION}/Nailed-{ART_NAME}-{ART_VERSION}.jar")
        }
        updateTask.setDependsOn(ImmutableSet.of("upload" + p.projectName))
        task.setUpdateTask(updateTask)
      case p: MavenArtifact =>
        val task = this.makeTask("update" + this.startUppercase(p.artifact), classOf[UpdateLibraryTask])
        if(p.localMavenPath == null){
          task.setDestination(if(p.mclib) "{MC_LIB_DIR}/" else "{NAILED_LIB_DIR}/" + this.parseMavenPath(p.mavenPath, classifier = false))
        }else{
          task.setDestination(if(p.mclib) "{MC_LIB_DIR}/" else "{NAILED_LIB_DIR}/" + this.parseMavenPath(p.localMavenPath, classifier = false))
        }
        task.setLocation(p.mavenServer + this.parseMavenPath(p.mavenPath))
        task.setArtifact(p.artifact)
        task.setRestart(p.restart)
        task.setUpdateTask(this.updateLibraryListTask)
        task.setFinalizedBy(ImmutableSet.of("updateLibraryList"))
        task.setLoad(p.load)
    }
  }

  def parseMavenPath(in: String, classifier: Boolean = true): String = {
    val extp = in.split("@", 2)
    val ext = if(extp.length == 2) extp(1) else "jar"
    val parts = in.split(":")
    val group = parts(0).replace('.', '/')
    val name = parts(1)
    val version = parts(2)
    var ret = group + "/"
    if(!name.isEmpty){
      ret += name + "/"
    }
    ret += version + "/"
    if(!name.isEmpty){
      ret += name + "-"
    }
    ret += version
    if(parts.length == 4 && classifier){
      ret += "-" + parts(3)
    }
    ret += "." + ext
    ret
  }

  def startUppercase(in: String) = Character.toUpperCase(in.charAt(0)) + in.substring(1)
}
