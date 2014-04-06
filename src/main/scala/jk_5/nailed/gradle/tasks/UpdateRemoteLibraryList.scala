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
import groovy.lang.Closure

/**
 * No description given
 *
 * @author jk-5
 */
class UpdateRemoteLibraryList extends DefaultTask {

  private var updated = mutable.ArrayBuffer[Library]()
  private var libraryList: LibraryList = _

  this.onlyIf(new Closure[Boolean](this, this){
    override def call(args: AnyRef*) = args(0).asInstanceOf[UpdateRemoteLibraryList].updated.size > 0
  })

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
      }
    })
    this.libraryList.tweakers = ext.getTweakers
    this.libraryList.versionName = ext.getVersionName
    val sftp = SshConnectionPool.getConnection(this.getProject)
    SshUtils.cd(sftp, ext.getRemoteProfileDir)
    sftp.put(new StringInputStream(Serialization.gson.toJson(this.libraryList)), Constants.REMOTE_VERSION_FILE)
    SshConnectionPool.cleanup()
  }

  def updateLibrary(library: Library) = this.updated += library
  def setLibraryList(libraryList: LibraryList) = this.libraryList = libraryList
}
