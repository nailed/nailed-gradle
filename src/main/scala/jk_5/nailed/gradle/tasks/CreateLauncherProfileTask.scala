package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import jk_5.nailed.gradle.delayed.{DelayedBase, DelayedFile, DelayedString}
import jk_5.nailed.gradle.extension.NailedExtension
import java.net.URL
import jk_5.nailed.gradle.Constants
import java.io.{FileWriter, InputStreamReader}
import com.google.gson._
import java.util
import java.util.Date
import jk_5.nailed.gradle.json.{LauncherProfile, Serialization, LauncherLibrary}
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConversions._

/**
 * No description given
 *
 * @author jk-5
 */
class CreateLauncherProfileTask extends DefaultTask {

  private var fmlJson: DelayedString = null
  private var destination: DelayedFile = null
  private val dependencies = new util.ArrayList[LauncherLibrary]
  private val tweakers = new util.ArrayList[String]

  @TaskAction def doTask(){
    val ext = NailedExtension.getInstance(getProject)
    val url = new URL(new DelayedString(this.getProject, Constants.FML_JSON_URL).call)
    val conn = url.openConnection
    conn.setConnectTimeout(5000)
    conn.setReadTimeout(5000)
    val reader = new InputStreamReader(conn.getInputStream)
    val fmlProfile = Serialization.gson.fromJson(new JsonParser().parse(reader).getAsJsonObject.getAsJsonObject("versionInfo"), classOf[LauncherProfile])
    val newProfile = new LauncherProfile
    reader.close()

    this.dependencies.foreach(newProfile.libraries.add)
    fmlProfile.libraries.filter(p => p.name != "@artifact@").foreach(l => {
      if(l.name.startsWith("org.scala-lang")){
        l.url = "http://maven.reening.nl/"
      }
      newProfile.libraries.add(l)
    })
    newProfile.libraries.foreach(l => {
      l.name = DelayedBase.resolve(l.name, this.getProject)
      if(l.url != null) l.url = DelayedBase.resolve(l.url, this.getProject)
    })

    val argsBuilder = new StringBuilder(fmlProfile.minecraftArguments.split(" --tweakClass ", 2)(0))
    this.tweakers.foreach(t => argsBuilder.append(" --tweakClass ").append(t))

    newProfile.id = ext.getVersionName
    newProfile.mainClass = ext.getMainClass
    newProfile.minimumLauncherVersion = fmlProfile.minimumLauncherVersion
    newProfile.incompatibilityReason = fmlProfile.incompatibilityReason
    newProfile.typ = "release"
    newProfile.time = new Date
    newProfile.releaseTime = new Date
    newProfile.sync = false
    newProfile.minecraftArguments = argsBuilder.toString()

    if(this.destination.call.isFile) this.destination.call.delete()
    val writer = new FileWriter(this.destination.call)
    Serialization.gson.toJson(newProfile, writer)
    writer.close()
  }

  def dependency(library: LauncherLibrary) = this.dependencies.add(library)
  def dependency(library: String): Unit = this.dependency(new LauncherLibrary(library))
  def dependency(library: String, url: String): Unit = this.dependency(new LauncherLibrary(library, url))
  def tweaker(tweaker: String): Unit = this.tweakers.add(tweaker)

  @inline def getFmlJson = this.fmlJson
  @inline def getDestination = this.destination
  @inline def setFmlJson(fmlJson: DelayedString) = this.fmlJson = fmlJson
  @inline def setDestination(destination: DelayedFile) = this.destination = destination
}
