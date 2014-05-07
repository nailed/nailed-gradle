package jk_5.nailed.gradle.common

import org.gradle.api.{Action, Plugin, Project}
import jk_5.nailed.gradle.delayed.{DelayedFile, DelayedString}
import org.gradle.api.artifacts.repositories.{IvyArtifactRepository, MavenArtifactRepository}
import java.util

/**
 * No description given
 *
 * @author jk-5
 */
trait BasePlugin extends Plugin[Project] {

  private var project: Project = null

  final def apply(project: Project) {
    this.project = project
    this.project.allprojects(new Action[Project] {
      def execute(project: Project) {
        addMavenRepo(project, "reening", "http://maven.reening.nl")
        addMavenRepo(project, "forge", "http://files.minecraftforge.net/maven")
        project.getRepositories.mavenCentral
        addMavenRepo(project, "minecraft", "https://libraries.minecraft.net")
        addIvyRepo(project, "forgeLegacy", "http://files.minecraftforge.net/[module]/[module]-dev-[revision].[ext]")
      }
    })
    project.afterEvaluate(new Action[Project] {
      def execute(project: Project) = BasePlugin.this.afterEvaluate()
    })
    this.applyPlugin()
  }

  def afterEvaluate()
  def applyPlugin()

  def addMavenRepo(project: Project, name: String, url: String) {
    project.getRepositories.maven(new Action[MavenArtifactRepository] {
      def execute(repo: MavenArtifactRepository) {
        repo.setName(name)
        repo.setUrl(url)
      }
    })
  }

  def addIvyRepo(project: Project, name: String, pattern: String) {
    project.getRepositories.ivy(new Action[IvyArtifactRepository] {
      def execute(repo: IvyArtifactRepository) {
        repo.setName(name)
        repo.artifactPattern(pattern)
      }
    })
  }

  @SuppressWarnings(Array("unchecked")) def makeTask[T](name: String, `type`: Class[T]): T = {
    val map: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
    map.put("name", name)
    map.put("type", `type`)
    this.getProject.task(map, name).asInstanceOf[T]
  }

  @inline implicit def stringToDelayedString(input: String): DelayedString = this.delayedString(input)
  @inline implicit def stringToDelayedFile(input: String): DelayedFile = this.delayedFile(input)
  @inline protected def delayedString(path: String) = new DelayedString(project, path)
  @inline protected def delayedFile(path: String) = new DelayedFile(project, path)
  @inline protected def getProject = this.project
}
