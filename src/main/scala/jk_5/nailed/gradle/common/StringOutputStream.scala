package jk_5.nailed.gradle.common

import java.io.OutputStream

/**
 * No description given
 *
 * @author jk-5
 */
class StringOutputStream extends OutputStream {

  private val buffer = new StringBuffer()

  override def write(b: Array[Byte]) = this.buffer.append(new String(b))
  override def write(b: Array[Byte], off: Int, len: Int) = this.buffer.append(new String(b, off, len))
  override def write(b: Int) = this.buffer.append(new String(Array[Byte](b.toByte)))
  override def toString = this.buffer.toString
}
