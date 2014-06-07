package jk_5.nailed.gradle.extension

import java.util
import org.gradle.api.Project
import jk_5.nailed.gradle.common.{UpdaterArtifact, MavenArtifact, DeployedArtifact}
import jk_5.nailed.gradle.json.RestartLevel

/**
 * No description given
 *
 * @author jk-5
 */
object NailedExtension {
  def getInstance(project: Project): NailedExtension = project.getExtensions.getByType(classOf[NailedExtension])
}

class NailedExtension(val project: Project) {

  private var minecraftVersion: String = null
  private var loadingMavenUrl: String = "http://maven.reening.nl/"
  private var deployHost: String = null
  private var deployUsername: String = null
  private var deployPassword: String = null
  private var remoteProfileDir: String = ""
  private val tweakers = new util.ArrayList[String]
  private val deployed = new util.ArrayList[UpdaterArtifact]

  @inline def getMinecraftVersion = this.minecraftVersion
  @inline def getLoadingMavenUrl = this.loadingMavenUrl
  @inline def getDeployHost = this.deployHost
  @inline def getDeployUsername = this.deployUsername
  @inline def getDeployPassword = this.deployPassword
  @inline def getRemoteProfileDir = this.remoteProfileDir
  @inline def getTweakers = this.tweakers
  @inline def getDeployed = this.deployed

  @inline def setMinecraftVersion(minecraftVersion: String) = this.minecraftVersion = minecraftVersion
  @inline def setLoadingMavenUrl(loadingMavenUrl: String) = this.loadingMavenUrl = loadingMavenUrl
  @inline def setDeployHost(deployHost: String) = this.deployHost = deployHost
  @inline def setDeployUsername(deployUsername: String) = this.deployUsername = deployUsername
  @inline def setDeployPassword(deployPassword: String) = this.deployPassword = deployPassword
  @inline def setRemoteProfileDir(remoteProfileDir: String) = this.remoteProfileDir = remoteProfileDir
  @inline def setTweaker(tweaker: String) = this.tweakers.add(tweaker)

  def setDeploy(data: util.Map[String, AnyRef]){
    if(data.containsKey("project")){
      val d = new DeployedArtifact
      d.artifact = data.get("artifact").toString
      d.projectName = data.get("project").toString
      d.mod = Option(data.get("mod")).getOrElse(false).toString.toBoolean
      d.load = Option(data.get("load")).getOrElse(false).toString.toBoolean
      d.reobf = Option(data.get("reobf")).getOrElse(false).toString.toBoolean
      d.coremod = if(data.containsKey("coremod")) data.get("coremod").toString else null
      d.restart = RestartLevel.valueOf(Option(data.get("restart")).getOrElse("nothing").toString.toUpperCase)
      d.mclib = Option(data.get("mclib")).getOrElse(false).toString.toBoolean
      this.deployed.add(d)
    }else if(data.containsKey("maven")){
      val d = new MavenArtifact
      d.artifact = data.get("artifact").toString
      d.mavenPath = data.get("maven").toString
      d.localMavenPath = if(data.containsKey("localmaven")) data.get("localmaven").toString else null
      d.mavenServer = data.get("server").toString
      d.mod = Option(data.get("mod")).getOrElse(false).toString.toBoolean
      d.load = Option(data.get("load")).getOrElse(false).toString.toBoolean
      d.reobf = Option(data.get("reobf")).getOrElse(false).toString.toBoolean
      d.coremod = if(data.containsKey("coremod")) data.get("coremod").toString else null
      d.restart = RestartLevel.valueOf(Option(data.get("restart")).getOrElse("nothing").toString.toUpperCase)
      d.mclib = Option(data.get("mclib")).getOrElse(false).toString.toBoolean
      this.deployed.add(d)
    }
  }
}
