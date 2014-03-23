package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import jk_5.nailed.gradle.delayed.DelayedString
import jk_5.nailed.gradle.extension.NailedExtension
import com.jcraft.jsch.Session
import com.google.gson.{JsonParser, JsonObject}
import java.io.InputStreamReader
import jk_5.nailed.gradle.Constants
import org.apache.tools.ant.filters.StringInputStream
import org.gradle.api.tasks.TaskAction
import groovy.lang.Closure
import jk_5.nailed.gradle.json.{Serialization, Library, LibraryList, RestartLevel}
import jk_5.nailed.gradle.common.SshConnectionPool

/**
 * No description given
 *
 * @author jk-5
 */
class UpdateAdditionalLibraryTask extends DefaultTask {

  private var destination: DelayedString = null
  private var location: DelayedString = null
  private var artifact: DelayedString = null
  private var restart = RestartLevel.NOTHING

  @TaskAction def doTask(){
    val sftp = SshConnectionPool.getConnection(this.getProject)
    sftp.cd(NailedExtension.getInstance(this.getProject).getRemoteProfileDir)
    val libList = LibraryList.readFromStream(sftp.get(Constants.REMOTE_VERSION_FILE))
    val libOption = libList.getArtifact(this.artifact.call)
    if(libOption.isEmpty){
      val l = new Library
      l.name = this.artifact.call
      libList.libraries.add(l)
    }
    val lib = libList.getArtifact(this.artifact.call).get
    lib.rev += 1
    lib.destination = this.destination.call
    lib.location = this.location.call
    lib.restart = this.restart
    lib.mod = false
    sftp.put(new StringInputStream(Serialization.gson.toJson(libList)), Constants.REMOTE_VERSION_FILE)
    SshConnectionPool.cleanup()
  }

  @inline def getDestination = this.destination
  @inline def getLocation = this.location
  @inline def getArtifact = this.artifact
  @inline def getRestart = this.restart

  @inline def setDestination(destination: DelayedString) = this.destination = destination
  @inline def setLocation(location: DelayedString) = this.location = location
  @inline def setArtifact(artifact: DelayedString) = this.artifact = artifact
  @inline def setRestart(restart: RestartLevel) = this.restart = restart
}
