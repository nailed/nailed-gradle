package jk_5.nailed.gradle.delayed

import org.gradle.api.Project

/**
 * No description given
 *
 * @author jk-5
 */
class DelayedString(owner: Project, pattern: String) extends DelayedBase[String](owner, pattern) {

  override def call: String = {
    if(this.resolved.isEmpty){
      this.resolved = Option(DelayedBase.resolve(pattern, project))
    }
    this.resolved.get
  }
}
