package jk_5.nailed.gradle.common

import com.jcraft.jsch.{SftpException, ChannelSftp, Session, JSch}
import org.gradle.api.Project
import jk_5.nailed.gradle.extension.NailedExtension

/**
 * This class pools sftp connections instead of reconnecting every time we need to do something.
 *
 * @author jk-5
 */
object SshConnectionPool {

  private val jsch = new JSch
  private var session: Session = _
  private var sftp: ChannelSftp = _

  def getConnection(project: Project): ChannelSftp = {
    if(sftp != null){
      return sftp
    }
    val ext = NailedExtension.getInstance(project)
    session = jsch.getSession(ext.getDeployUsername, ext.getDeployHost)
    session.setPassword(ext.getDeployPassword)
    session.setConfig("StrictHostKeyChecking", "no")
    session.connect()
    val channel = session.openChannel("sftp")
    channel.connect()
    sftp = channel.asInstanceOf[ChannelSftp]
    sftp
  }

  def close(){
    if(this.sftp != null && !this.sftp.isClosed){
      this.sftp.exit()
      this.session.disconnect()
      this.sftp = null
    }else if(this.sftp != null && this.sftp.isClosed){
      this.sftp = null
    }
  }

  @inline def cleanup() = this.sftp.cd(this.sftp.getHome)
}

object SshUtils {

  def cd(sftp: ChannelSftp, dir: String){
    dir.split("/").foreach(folder => {
      if(folder.length > 0){
        try{
          sftp.cd(folder)
        }catch{
          case e: SftpException =>
            sftp.mkdir(folder)
            sftp.cd(folder)
        }
      }
    })
  }
}
