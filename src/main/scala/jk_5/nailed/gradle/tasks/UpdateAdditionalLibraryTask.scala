package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import jk_5.nailed.gradle.delayed.DelayedString
import org.gradle.api.tasks.TaskAction
import jk_5.nailed.gradle.json.{Library, RestartLevel}

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
  private var updateTask: UpdateRemoteLibraryList = null

  @TaskAction def doTask(){
    val lib = new Library
    lib.destination = this.destination.call
    lib.location = this.location.call
    lib.restart = this.restart
    lib.mod = false
    lib.name = this.artifact.call
    this.updateTask.updateLibrary(lib)
  }

  @inline def getDestination = this.destination
  @inline def getLocation = this.location
  @inline def getArtifact = this.artifact
  @inline def getRestart = this.restart

  @inline def setDestination(destination: DelayedString) = this.destination = destination
  @inline def setLocation(location: DelayedString) = this.location = location
  @inline def setArtifact(artifact: DelayedString) = this.artifact = artifact
  @inline def setRestart(restart: RestartLevel) = this.restart = restart
  @inline def setUpdateTask(updateTask: UpdateRemoteLibraryList) = this.updateTask = updateTask
}
