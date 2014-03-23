package jk_5.nailed.gradle.json

import java.io._
import java.util
import com.google.common.collect.Lists
import scala.collection.JavaConversions._

/**
 * No description given
 *
 * @author jk-5
 */
class Library {
  var name: String = _
  var rev = 0
  var destination: String = _
  var location: String = _
  var restart = RestartLevel.NOTHING
  var mod = false
}

class LibraryList {
  var versionName: String = _
  var libraries: util.List[Library] = _ //We need a java list here. Gson doesn't know how to handle scala collections

  def getArtifact(name: String): Option[Library] = this.libraries.find(_.name == name)
}

object LibraryList {
  def readFromStream(stream: InputStream): LibraryList = {
    try{
      Serialization.gson.fromJson(new InputStreamReader(stream), classOf[LibraryList])
    }catch{
      case e: Exception => return {
        val l = new LibraryList
        l.libraries = Lists.newArrayList()
        l
      }
    }
  }
}
