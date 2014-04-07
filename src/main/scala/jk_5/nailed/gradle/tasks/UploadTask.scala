package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import jk_5.nailed.gradle.delayed.{DelayedString, DelayedFile}
import jk_5.nailed.gradle.extension.NailedExtension
import java.io.FileInputStream
import org.gradle.api.tasks.TaskAction
import jk_5.nailed.gradle.common.{SshUtils, SshConnectionPool}
import jk_5.nailed.gradle.json.RestartLevel

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
  private var coremod: DelayedString = null
  private var restart = RestartLevel.NOTHING
  private var mod = false
  private var load = false
  private var updateTask: UpdateLibraryTask = null

  @TaskAction def doTask(){
    val ext = NailedExtension.getInstance(this.getProject)
    val sftp = SshConnectionPool.getConnection(this.getProject)

    SshUtils.cd(sftp, this.remoteDir.call)
    sftp.put(new FileInputStream(this.getUploadFile.call), this.remoteFile.call)

    SshConnectionPool.cleanup()

    if(this.updateTask != null){
      this.updateTask.setDestination(this.destination)
      this.updateTask.setLocation(new DelayedString(this.getProject, ext.getLoadingMavenUrl + this.remoteDir.call + "/" + this.remoteFile.call))
      this.updateTask.setRestart(this.restart)
      this.updateTask.setMod(this.mod)
      this.updateTask.setLoad(this.load)
      this.updateTask.setCoremod(this.coremod)
      this.updateTask.setArtifact(this.artifact)
    }
  }

  @inline def getUploadFile = this.uploadFile
  @inline def getRemoteDir = this.remoteDir
  @inline def getRemoteFile = this.remoteFile
  @inline def getDestination = this.destination
  @inline def getArtifact = this.artifact
  @inline def getRestart = this.restart
  @inline def getCoremod = this.coremod
  @inline def isMod = this.mod
  @inline def isLoad = this.load

  @inline def setUploadFile(uploadFile: DelayedFile) = this.uploadFile = uploadFile
  @inline def setRemoteDir(remoteDir: DelayedString) = this.remoteDir = remoteDir
  @inline def setRemoteFile(remoteFile: DelayedString) = this.remoteFile = remoteFile
  @inline def setDestination(destination: DelayedString) = this.destination = destination
  @inline def setArtifact(artifact: DelayedString) = this.artifact = artifact
  @inline def setRestart(restart: RestartLevel) = this.restart = restart
  @inline def setIsMod(isMod: Boolean) = this.mod = isMod
  //@inline def setUpdateTask(updateTask: UpdateRemoteLibraryList) = this.updateTask = updateTask
  @inline def setLoad(load: Boolean) = this.load = load
  @inline def setCoremod(coremod: DelayedString) = this.coremod = coremod
  @inline def setUpdateTask(updateTask: UpdateLibraryTask) = this.updateTask = updateTask
}
