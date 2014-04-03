package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import jk_5.nailed.gradle.delayed.{DelayedString, DelayedFile}
import jk_5.nailed.gradle.extension.NailedExtension
import jk_5.nailed.gradle.Constants
import java.io.FileInputStream
import org.apache.tools.ant.filters.StringInputStream
import org.gradle.api.tasks.TaskAction
import jk_5.nailed.gradle.common.{SshUtils, SshConnectionPool}
import jk_5.nailed.gradle.json.{Serialization, Library, LibraryList, RestartLevel}

/**
 * No description given
 *
 * @author jk-5
 */
class UploadTask extends DefaultTask {

  private var uploadFile: DelayedFile = null
  private var remoteDir: DelayedString = null
  private var remoteFile: DelayedString = null
  private var destination: DelayedString = null
  private var artifact: DelayedString = null
  private var restart = RestartLevel.NOTHING
  private var mod = false

  @TaskAction def doTask(){
    val ext = NailedExtension.getInstance(this.getProject)
    val sftp = SshConnectionPool.getConnection(this.getProject)

    SshUtils.cd(sftp, this.remoteDir.call)

    sftp.put(new FileInputStream(this.getUploadFile.call), this.remoteFile.call)
    sftp.cd(sftp.getHome)
    sftp.cd(ext.getRemoteProfileDir)

    val libList = LibraryList.readFromStream(sftp.get(Constants.REMOTE_VERSION_FILE))
    val libOption = libList.getArtifact(this.artifact.call)
    libList.tweakers = ext.getTweakers
    if(libOption.isEmpty){
      val l = new Library
      l.name = this.artifact.call
      libList.libraries.add(l)
    }
    val lib = libList.getArtifact(this.artifact.call).get
    lib.rev += 1
    lib.destination = this.destination.call
    lib.location = ext.getLoadingMavenUrl + this.remoteDir.call + "/" + this.remoteFile.call
    lib.restart = this.restart
    lib.mod = this.mod
    sftp.put(new StringInputStream(Serialization.gson.toJson(libList)), Constants.REMOTE_VERSION_FILE)
    SshConnectionPool.cleanup()
  }

  @inline def getUploadFile = this.uploadFile
  @inline def getRemoteDir = this.remoteDir
  @inline def getRemoteFile = this.remoteFile
  @inline def getDestination = this.destination
  @inline def getArtifact = this.artifact
  @inline def getRestart = this.restart
  @inline def isMod = this.mod

  @inline def setUploadFile(uploadFile: DelayedFile) = this.uploadFile = uploadFile
  @inline def setRemoteDir(remoteDir: DelayedString) = this.remoteDir = remoteDir
  @inline def setRemoteFile(remoteFile: DelayedString) = this.remoteFile = remoteFile
  @inline def setDestination(destination: DelayedString) = this.destination = destination
  @inline def setArtifact(artifact: DelayedString) = this.artifact = artifact
  @inline def setRestart(restart: RestartLevel) = this.restart = restart
  @inline def setIsMod(isMod: Boolean) = this.mod = isMod
}
