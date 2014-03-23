package jk_5.nailed.gradle.json

import java.util
import java.util.Date
import com.google.gson.annotations.SerializedName
import com.google.common.collect.Lists
import jk_5.nailed.gradle.Constants

/**
 * No description given
 *
 * @author jk-5
 */
class LauncherProfile{

  var id: String = _
  var mainClass: String = _
  var minimumLauncherVersion: Int = _
  var incompatibilityReason: String = _
  @SerializedName("type") var typ: String = _
  var time: Date = _
  var releaseTime: Date = _
  var sync: Boolean = _
  var minecraftArguments: String = _
  var libraries: util.List[LauncherLibrary] = Lists.newArrayList()
  var assets: String = _
  var rules: util.List[OSRule] = _
}

class OSRule{

  var action = Action.ALLOW
  var os: OSInfo = _

  class OSInfo {
    var name: OS = _
    var version: String = _
  }
}

class LauncherLibrary(var name: String = null, var url: String = null){

  var rules: util.List[OSRule] = _
  var extract: ExtractRule = _
  var natives: util.Map[OS, String] = _
  @transient private var artifact: Artifact = null

  def getPath: String = {
    if(this.artifact == null) this.artifact = new Artifact(this.name)
    this.artifact.getPath
  }

  def getArtifactName: String = {
    if(this.artifact == null) this.artifact = new Artifact(this.name)
    this.artifact.getArtifact
  }

  @inline def getUrl = if(this.url == null) Constants.MINECRAFT_MAVEN else this.url
  @inline override def toString = this.name

  private class Artifact(rep: String){

    @transient private val pts = rep.split(":")
    @transient private val idx = pts(pts.length - 1).indexOf('@')
    private var ext: String = {
      if(idx != -1){
        val ret = pts(pts.length - 1).substring(idx + 1)
        pts(pts.length - 1) = pts(pts.length - 1).substring(0, idx)
        ret
      }else{
        "jar"
      }
    }
    private var domain = pts(0)
    private var name = pts(1)
    private var version = pts(2)
    private var classifier = if (pts.length > 3) pts(3) else null

    def getArtifact: String = this.getArtifact(this.classifier)
    def getArtifact(classifier: String): String = {
      var ret = this.domain + ":" + this.name + ":" + this.version
      if(classifier != null) ret += ":" + classifier
      if(this.ext != "jar") ret += "@" + this.ext
      ret
    }
    def getPath: String = this.getPath(this.classifier)
    def getPath(classifier: String): String = {
      var ret = "%s/%s/%s/%s-%s".format(this.domain.replace('.', '/'), this.name, this.version, this.name, this.version)
      if(classifier != null) ret += "-" + classifier
      ret + "." + this.ext
    }
  }
}

class ExtractRule{
  var exclude: util.List[String] = _
}
