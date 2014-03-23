package jk_5.nailed.gradle.extension

import java.util
import org.gradle.api.Project
import scala.collection.JavaConversions._

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
  private var forgeVersion: String = null
  private var versionName: String = "Nailed"
  private var loadingMavenUrl: String = "http://maven.reening.nl/"
  private var mainClass: String = null
  private var deployHost: String = null
  private var deployUsername: String = null
  private var deployPassword: String = null
  private var remoteProfileDir: String = ""
  private val tweakers = new util.ArrayList[String]
  private val deployed = new util.ArrayList[Project]
  private val deployedMods = new util.ArrayList[Project]
  private val additionalLibs = new util.ArrayList[String]

  @inline def getMinecraftVersion = this.minecraftVersion
  @inline def getForgeVersion = this.forgeVersion
  @inline def getVersionName = this.versionName
  @inline def getLoadingMavenUrl = this.loadingMavenUrl
  @inline def getMainClass = this.mainClass
  @inline def getDeployHost = this.deployHost
  @inline def getDeployUsername = this.deployUsername
  @inline def getDeployPassword = this.deployPassword
  @inline def getRemoteProfileDir = this.remoteProfileDir
  @inline def getTweakers = this.tweakers
  @inline def getDeployed = this.deployed
  @inline def getDeployedMods = this.deployedMods
  @inline def getAdditionalLibs = this.additionalLibs

  @inline def setMinecraftVersion(minecraftVersion: String) = this.minecraftVersion = minecraftVersion
  @inline def setForgeVersion(forgeVersion: String) = this.forgeVersion = forgeVersion
  @inline def setVersionName(versionName: String) = this.versionName = versionName
  @inline def setLoadingMavenUrl(loadingMavenUrl: String) = this.loadingMavenUrl = loadingMavenUrl
  @inline def setMainClass(mainClass: String) = this.mainClass = mainClass
  @inline def setDeployHost(deployHost: String) = this.deployHost = deployHost
  @inline def setDeployUsername(deployUsername: String) = this.deployUsername = deployUsername
  @inline def setDeployPassword(deployPassword: String) = this.deployPassword = deployPassword
  @inline def setRemoteProfileDir(remoteProfileDir: String) = this.remoteProfileDir = remoteProfileDir
  @inline def setTweaker(tweaker: String) = this.tweakers.add(tweaker)
  @inline def setDeployed(deployed: String) = this.project.getSubprojects.filter(_.getName == deployed).foreach(this.deployed.add)
  @inline def setDeployedMod(deployedMod: String) = this.project.getSubprojects.filter(_.getName == deployedMod).foreach(this.deployedMods.add)
  @inline def setAdditionalLib(additionalLib: String) = this.additionalLibs.add(additionalLib)
}
