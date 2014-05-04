package jk_5.nailed.gradle.delayed

import groovy.lang.Closure
import org.gradle.api.Project
import jk_5.nailed.gradle.extension.NailedExtension

/**
 * No description given
 *
 * @author jk-5
 */
object DelayedBase {

  def resolve(p: String, project: Project): String = {
    var pattern = p
    project.getLogger.info("Resolving: " + pattern, null, null)
    if(p == null) return null
    var build: String = "0"
    if (System.getenv.containsKey("BUILD_NUMBER")) {
      build = System.getenv("BUILD_NUMBER")
    }
    val ext: NailedExtension = NailedExtension.getInstance(project)
    pattern = pattern.replace("{MC_VERSION}", ext.getMinecraftVersion)
    pattern = pattern.replace("{CACHE_DIR}", project.getGradle.getGradleUserHomeDir.getAbsolutePath.replace('\\', '/') + "/caches/nailed-forge")
    pattern = pattern.replace("{BUILD_DIR}", project.getBuildDir.getAbsolutePath.replace('\\', '/'))
    pattern = pattern.replace("{PROJECT}", project.getName)
    pattern = pattern.replace("{VERSION}", project.getVersion.toString)
    pattern = pattern.replace("{GROUP_ID}", project.getGroup.toString)
    pattern = pattern.replace("{GROUP_DIR}", project.getGroup.toString.replace('.', '/'))
    pattern = pattern.replace("{REMOTE_DATA_DIR}", ext.getRemoteProfileDir)
    pattern = pattern.replace("{MAVEN_URL}", ext.getLoadingMavenUrl)
    project.getLogger.info("Resolved: " + pattern, null, null)
    pattern
  }
}

abstract class DelayedBase[T](protected val project: Project, protected val pattern: String) extends Closure[T](project){

  protected var resolved: Option[T] = None

  def call: T
  override def toString = call.toString
}
