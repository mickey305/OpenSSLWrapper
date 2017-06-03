
import java.io.File

import com.mickey.openssl.wrapper.EncryptionFactory
import com.mickey305.util.cli.model.Benchmark

/**
  * Created by K.Misaki on 2017/06/01.
  *
  */
object SampleMain extends App {
  var home = System.getenv("HOME")
  home = if (home.endsWith(File.separator)) home else home + File.separator

  val encryption = new EncryptionFactory
  val encryptionTaskManager = encryption.encryptionTaskManager
  val opensslConfig = encryptionTaskManager.opensslConfig
  val privateKeyPath = home + "Desktop/sample/test_private.pem"
  val publicKeyPath = home + "Desktop/sample/test_pub.pem"
//  encryptionTaskManager.cancelAll()
//  encryptionTaskManager.subscribePrivateKeyCreator(privateKeyPath)
//  encryptionTaskManager.subscribePublicKeyCreator(privateKeyPath, publicKeyPath)
//  encryptionTaskManager.invoke()
  opensslConfig.publicKey = publicKeyPath
  opensslConfig.privateKey = privateKeyPath

  // encryption
  encryption.packageFilePath = home + "Desktop/sample/out/Test.encbox"
  encryption.pack(home + "Desktop/sample/Test.zip")
  // decryption
  encryption.packageFilePath = home + "Desktop/sample/out/Test.encbox"
  encryption.unpack(home + "Desktop/sample/out")

  encryption.encryptionTaskManager.journal().foreach(journal => {
    val timestamps = journal.getTimestampMaps
    val time = timestamps.get(Benchmark.END).getTime - timestamps.get(Benchmark.START).getTime
    println(time.toString + " [ms] " + journal.getExecutionSentence)
  })
  val times = encryption.encryptionTaskManager.journal().map(journal => {
    val timestamps = journal.getTimestampMaps
    timestamps.get(Benchmark.END).getTime - timestamps.get(Benchmark.START).getTime})
  println("total - " + times.sum + " [ms]")
}
