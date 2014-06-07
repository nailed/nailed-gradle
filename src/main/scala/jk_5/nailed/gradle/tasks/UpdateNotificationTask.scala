package jk_5.nailed.gradle.tasks

import org.gradle.api.{Task, DefaultTask}
import org.gradle.api.tasks.TaskAction
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import java.net.URLEncoder
import org.gradle.api.specs.Spec

/**
 * No description given
 *
 * @author jk-5
 */
class UpdateNotificationTask extends DefaultTask {

  var hookUrl: String = null
  var updateTask: UpdateRemoteLibraryListTask = null

  this.onlyIf(new Spec[Task]{
    override def isSatisfiedBy(element: Task): Boolean = element.asInstanceOf[UpdateNotificationTask].hookUrl != null
  })

  @TaskAction def doTask(){
    var payload = "[\u000302nailed-forge\u000f] \u000303" + updateTask.getUpdated.size() + "\u000f new updates were deployed"
    new DefaultHttpClient().execute(new HttpGet(this.hookUrl + "?payload=" + URLEncoder.encode(payload, "UTF-8")))
    payload = "[\u000302nailed-forge\u000f] " + updateTask.getUpdated.map(_.name).mkString(", ")
    new DefaultHttpClient().execute(new HttpGet(this.hookUrl + "?payload=" + URLEncoder.encode(payload, "UTF-8")))
  }

  def setHookUrl(hookUrl: String) = this.hookUrl = hookUrl
  def getHookUrl = this.hookUrl
  def setUpdateTask(updateTask: UpdateRemoteLibraryListTask) = this.updateTask = updateTask
  def getUpdateTask = this.updateTask
}
