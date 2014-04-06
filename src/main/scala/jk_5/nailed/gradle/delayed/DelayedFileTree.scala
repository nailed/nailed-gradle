package jk_5.nailed.gradle.delayed

import org.gradle.api.file.FileTree
import org.gradle.api.Project

/**
 * No description given
 *
 * @author jk-5
 */
class DelayedFileTree(owner: Project, pattern: String) extends DelayedBase[FileTree](owner, pattern) {

  override def call: FileTree = {
    if(this.resolved.isEmpty){
      this.resolved = Option(project.fileTree(DelayedBase.resolve(pattern, project)))
    }
    this.resolved.getOrElse(null)
  }
}
