package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import jk_5.nailed.gradle.json.{Serialization, LibraryList, Library}
import scala.collection.mutable
import scala.collection.JavaConversions._
import jk_5.nailed.gradle.common.{SshUtils, SshConnectionPool}
import jk_5.nailed.gradle.extension.NailedExtension
import org.apache.tools.ant.filters.StringInputStream
import jk_5.nailed.gradle.Constants

/**
 * No description given
 *
 * @author jk-5
 */
class UpdateRemoteLibraryListTask extends DefaultTask {

  private var updated = mutable.ArrayBuffer[Library]()
  private var libraryList: LibraryList = _
  private var launcherProfileTask: CreateLauncherProfileTask = _

  @TaskAction def doTask(){
    val ext = NailedExtension.getInstance(this.getProject)
    this.updated.foreach(library => {
      val local = this.libraryList.libraries.find(_.name == library.name)
      if(local.isEmpty){
        this.libraryList.libraries.add(library)
      }else{
        val l = local.get
        l.rev += 1
        l.destination = library.destination
        l.location = library.location
        l.restart = library.restart
        l.mod = library.mod
        l.load = library.load
        l.coremod = library.coremod
      }
    })
    this.libraryList.tweakers = ext.getTweakers
    this.libraryList.versionName = this.launcherProfileTask.getVersionName
    val sftp = SshConnectionPool.getConnection(this.getProject)
    SshUtils.cd(sftp, ext.getRemoteProfileDir)
    sftp.put(new StringInputStream(Serialization.gson.toJson(this.libraryList)), Constants.REMOTE_VERSION_FILE)
    SshConnectionPool.cleanup()
  }

  def updateLibrary(library: Library) = this.updated += library
  def setLibraryList(libraryList: LibraryList) = this.libraryList = libraryList
  def setLauncherProfileTask(task: CreateLauncherProfileTask) = this.launcherProfileTask = task
  def getLibraries = this.libraryList.libraries
  def getUpdated = this.updated
}
