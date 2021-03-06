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
class UpdateLibraryTask extends DefaultTask {

  private var destination: DelayedString = null
  private var location: DelayedString = null
  private var artifact: DelayedString = null
  private var coremod: DelayedString = null
  private var restart = RestartLevel.NOTHING
  private var load = false
  private var mod = false
  private var updateTask: UpdateRemoteLibraryListTask = null

  @TaskAction def doTask(){
    val lib = new Library
    lib.destination = this.destination.call
    lib.location = this.location.call
    lib.restart = this.restart
    lib.mod = this.mod
    lib.load = this.load
    lib.name = this.artifact.call
    lib.coremod = if(this.coremod == null) null else this.coremod.call
    this.updateTask.updateLibrary(lib)
  }

  @inline def getDestination = this.destination
  @inline def getLocation = this.location
  @inline def getArtifact = this.artifact
  @inline def getRestart = this.restart
  @inline def getCoremod = this.coremod
  @inline def isLoad = this.load

  @inline def setDestination(destination: DelayedString) = this.destination = destination
  @inline def setLocation(location: DelayedString) = this.location = location
  @inline def setArtifact(artifact: DelayedString) = this.artifact = artifact
  @inline def setRestart(restart: RestartLevel) = this.restart = restart
  @inline def setUpdateTask(updateTask: UpdateRemoteLibraryListTask) = this.updateTask = updateTask
  @inline def setLoad(load: Boolean) = this.load = load
  @inline def setMod(mod: Boolean) = this.mod = mod
  @inline def setCoremod(coremod: DelayedString) = this.coremod = coremod
}
