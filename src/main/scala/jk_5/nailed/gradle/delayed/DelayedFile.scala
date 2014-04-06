package jk_5.nailed.gradle.delayed

import java.io.File
import org.gradle.api.Project

/**
 * No description given
 *
 * @author jk-5
 */
class DelayedFile(owner: Project, pattern: String) extends DelayedBase[File](owner, pattern) {

  override def call: File = {
    if(this.resolved.isEmpty){
      this.resolved = Option(project.file(DelayedBase.resolve(pattern, project)))
    }
    this.resolved.getOrElse(null)
  }
}
