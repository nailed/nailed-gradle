package jk_5.nailed.gradle.tasks

import org.gradle.api.Project
import jk_5.nailed.gradle.delayed.{DelayedFile, DelayedString}

/**
 * No description given
 *
 * @author jk-5
 */
class UploadSubprojectTask extends UploadTask {

  private var subProject: Project = null

  override def doTask(){
    val name = this.subProject.getName
    val version = this.subProject.getVersion.toString
    val groupDir = this.subProject.getGroup.toString.replace('.', '/')
    this.setRemoteDir(this.delayedString(groupDir + "/Nailed-" + name + "/" + version))
    this.setRemoteFile(this.delayedString("Nailed-" + name + "-" + version + ".jar"))
    this.setUploadFile(this.delayedFile("{BUILD_DIR}/libs/Nailed-" + name + "-" + version + ".jar"))
    this.setDestination(this.delayedString(this.getDestination.call.replace("{ART_GROUP}", groupDir).replace("{ART_NAME}", name).replace("{ART_VERSION}", version)))
    super.doTask()
  }

  @inline private def delayedString(string: String) = new DelayedString(this.getProject, string)
  @inline private def delayedFile(string: String) = new DelayedFile(this.getProject, string)
  @inline def setSubProject(subProject: Project) = this.subProject = subProject
}
