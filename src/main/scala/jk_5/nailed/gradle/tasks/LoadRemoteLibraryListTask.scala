package jk_5.nailed.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import jk_5.nailed.gradle.common.{StringOutputStream, SshUtils, SshConnectionPool}
import jk_5.nailed.gradle.extension.NailedExtension
import jk_5.nailed.gradle.Constants
import jk_5.nailed.gradle.json.{LibraryList, Serialization}

/**
 * No description given
 *
 * @author jk-5
 */
class LoadRemoteLibraryListTask extends DefaultTask {

  private var updateTask: UpdateRemoteLibraryList = _

  @TaskAction def doTask(){
    val sftp = SshConnectionPool.getConnection(this.getProject)
    SshUtils.cd(sftp, NailedExtension.getInstance(this.getProject).getRemoteProfileDir)

    val output = new StringOutputStream
    sftp.get(Constants.REMOTE_VERSION_FILE, output)
    this.updateTask.setLibraryList(Serialization.gson.fromJson(output.toString, classOf[LibraryList]))
    SshConnectionPool.cleanup()
  }

  def setUpdateTask(updateTask: UpdateRemoteLibraryList) = this.updateTask = updateTask
}
