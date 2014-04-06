package jk_5.nailed.gradle.common

import jk_5.nailed.gradle.json.RestartLevel

/**
 * No description given
 *
 * @author jk-5
 */
case class DeployedArtifact(
  var name: String,
  var mod: Boolean,
  var load: Boolean,
  var coremod: String,
  var reobf: Boolean = false,
  var restart: RestartLevel
)
