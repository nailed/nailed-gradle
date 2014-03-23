package jk_5.nailed.gradle.common

import java.io.{File, FileInputStream}
import java.security.{DigestInputStream, MessageDigest}
import org.apache.commons.io.IOUtils
import java.math.BigInteger

/**
 * No description given
 *
 * @author jk-5
 */
object HashUtils {

  def hash(file: File): String = hash(file, "MD5")
  def hash(file: File, function: String): String = {
    try{
      val fis = new FileInputStream(file)
      val buffer = new Array[Byte](1024)
      val complete = MessageDigest.getInstance(function)
      var numRead = 0
      do{
        numRead = fis.read(buffer)
        if(numRead > 0) {
          complete.update(buffer, 0, numRead)
        }
      }while(numRead != -1)
      fis.close()

      val hash = complete.digest
      var result: String = ""
      for(i <- 0 until hash.length){
        result += Integer.toString((hash(i) & 0xff) + 0x100, 16).substring(1)
      }
      return result
    }catch{
      case e: Exception => e.printStackTrace()
    }
    null
  }

  def hash(str: String): String = {
    try{
      val complete = MessageDigest.getInstance("MD5")
      val hash = complete.digest(str.getBytes)
      var result = ""
      for(i <- 0 until hash.length){
        result += Integer.toString((hash(i) & 0xff) + 0x100, 16).substring(1)
      }
      return result
    }catch{
      case e: Exception => e.printStackTrace()
    }
    null
  }

  def getSHA1(file: File): String = this.getDigest(file, "SHA-1", 40)

  def getDigest(file: File, algorithm: String, hashLength: Int): String = {
    var stream: DigestInputStream = null
    try{
      stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm))
      var read = 0
      val buffer = new Array[Byte](65536)
      do read = stream.read(buffer) while(read > 0)
    }catch{
      case ignored: Exception => return null
    }finally {
      IOUtils.closeQuietly(stream)
    }
    ("%1$0" + hashLength + "x").format(new BigInteger(1, stream.getMessageDigest.digest))
  }
}
