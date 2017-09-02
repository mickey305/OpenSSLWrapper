
import java.io.File

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.mickey305.util.cli.model.Benchmark

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.ask
import com.mickey305.openssl.wrapper.EncryptionFactory
import com.mickey305.openssl.wrapper.actor.utils.IdCache

/**
  * Created by K.Misaki on 2017/06/01.
  *
  */
object SampleMain extends App {
  val THREAD_NUM = 60

  var home = System.getProperty("user.home")
  home = if (home.endsWith(File.separator)) home else home + File.separator

  // clean test output contents
  val outFiles = new File(home + "Desktop/sample/out/").listFiles()
  outFiles.filter(outFiles => outFiles.getCanonicalFile.getName.endsWith(".encbox")
    || outFiles.getCanonicalFile.getName.endsWith(".zip"))
    .foreach(outFile => outFile.delete())

  implicit val timeout = Timeout(60.seconds)
  var futures = List[Future[Long]]()
  var times = List[Long]()
  val system = ActorSystem("sampleSystem")
  val props = Props(classOf[SampleActor], home)
  var totalSkipCnt = 0

  IdCache.initTmpDirNameSet()

  try {
    0.until(THREAD_NUM).foreach(i => {
      val no = i + 1
      val actor = system.actorOf(props, name = "sampleActor" + no)
      futures :+= ask(actor, no).mapTo[Long]
    })

    futures.foreach(future => {
      times :+= Await.result(future, timeout.duration)
    })

    println("-----------------------------------------------------------------------------------------------------------")
    times.zipWithIndex.foreach { case (total, index) => {
      println("No." + (index + 1) + " - total " + total + " [ms]")
      if (total == 0) totalSkipCnt += 1
    }}
    println("-----------------------------------------------------------------------------------------------------------")
    val size = times.size - totalSkipCnt
    println(size + " times total   - " + times.sum + " [ms]")
    println(size + " times average - " + (times.sum.toFloat / size) + " [ms]")

  } finally {
    // shutdown actor system
    system.terminate
  }
}

class SampleActor(homeDir: String) extends Actor {
  override def preStart = {
//    println("sampleActor started.")
  }
  override def receive = {
    case "test"      => sender ! testEncryptionAndDecryption()
    case number: Int => sender ! synchronized { testEncryptionAndDecryption(number) }
    case _           => sender ! 0
  }
  override def postStop = {
//    println("sampleActor is stopped.")
  }

  def testEncryptionAndDecryption(number: Long): Long = {
    val index = if (number == 0) "" else "No." + number.toString
    println("+-- Invoke Thread " + index + " --+")

    val encryption = new EncryptionFactory
    val encryptionTaskManager = encryption.cliTskMngr
    val opensslConfig = encryption.opensslCfg
    val privateKeyPath = homeDir + "Desktop/sample/test_private.pem"
    val publicKeyPath = homeDir + "Desktop/sample/test_pub.pem"
//    encryptionTaskManager.cancelAll()
//    encryptionTaskManager.subscribePrivateKeyCreator(privateKeyPath)
//    encryptionTaskManager.subscribePublicKeyCreator(privateKeyPath, publicKeyPath)
//    encryptionTaskManager.invoke()
    opensslConfig.publicKey = publicKeyPath
    opensslConfig.privateKey = privateKeyPath

    var packagePath = homeDir + "Desktop/sample/out/Test.encbox"
    val status = encryptionTaskManager.cmdStatus

    // encryption
    if (status) {
      encryption.packageFilePath = packagePath
      encryption.pack(homeDir + "Desktop/sample/Test.zip")
    }

    // decryption
    if (status) {
      packagePath = new File(packagePath).getParentFile
        .listFiles()
        .sortWith((a, b) => a.lastModified() > b.lastModified()).head.getCanonicalPath
      encryption.packageFilePath = packagePath
      encryption.unpack(homeDir + "Desktop/sample/out")
    }

    val journals = encryptionTaskManager.journal()

    // log output
    println
    journals.foreach(journal => {
      val timestamps = journal.getTimestampMaps
      val time = timestamps.get(Benchmark.END).getTime - timestamps.get(Benchmark.START).getTime
      println(index + " ---> " + time.toString + " [ms] " + journal.getExecutionSentence)
    })
    println(index + " ---> " + journals.size + " commands invoked!")
    println

    // return
    synchronized {
      journals.map(journal => {
        val timestamps = journal.getTimestampMaps
        timestamps.get(Benchmark.END).getTime - timestamps.get(Benchmark.START).getTime
      }).sum
    }
  }

  def testEncryptionAndDecryption(): Long = {
    testEncryptionAndDecryption(0)
  }
}
