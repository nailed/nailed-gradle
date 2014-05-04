package jk_5.nailed.gradle.common

import jk_5.nailed.gradle.json.RestartLevel

/**
 * No description given
 *
 * @author jk-5
 */
case class DeployedArtifact(
  var artifact: String = null,
  var projectName: String = null,
  var mod: Boolean = false,
  var load: Boolean = false,
  var coremod: String = null,
  var reobf: Boolean = false,
  var restart: RestartLevel = RestartLevel.NOTHING,
  var mclib: Boolean = false
) extends UpdaterArtifact
case class MavenArtifact(
  var artifact: String = null,
  var mavenServer: String = null,
  var mavenPath: String = null,
  var localMavenPath: String = null,
  var mod: Boolean = false,
  var load: Boolean = false,
  var coremod: String = null,
  var reobf: Boolean = false,
  var restart: RestartLevel = RestartLevel.NOTHING,
  var mclib: Boolean = false
) extends UpdaterArtifact
trait UpdaterArtifact
