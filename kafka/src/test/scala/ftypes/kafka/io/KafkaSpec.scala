package ftypes.kafka.io

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import ftypes.kafka.{KafkaConsumer, KafkaDsl, SerializerImplicits}
import ftypes.log.PrintLog
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class KafkaSpec extends FlatSpec with Matchers with Eventually with SerializerImplicits with BeforeAndAfterAll with KafkaDsl {

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(500.millis, 250.millis)

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  val lastMessage = new AtomicReference[String]()
  val topic = "test"
  
  implicit val L = PrintLog[IO]

  val kafka = KafkaMock[IO](topic)

  val fn = KafkaConsumer[IO] {
    case msg @ Topic("test") => for {
      _ <- L.info(s"Consuming message ${msg.offset} from ${msg.topic}")
      s <- msg.as[String]
      _ <- IO {
        lastMessage.set(s)
      }
    } yield ()
  }

  override def beforeAll(): Unit = {
    kafka.mountConsumer(fn).unsafeToFuture()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    kafka.stop.unsafeRunSync()
    super.afterAll()
  }

  it should "produce and consume a message" ignore {
    val test = for {
      _ <- kafka.produce(topic, "Hello, World!")
      _ <- L.info("Wait 100ms")
      _ <- IO(Thread.sleep(100))
      _ <- L.warn("Bye")
    } yield ()

    test.unsafeRunSync()

    lastMessage.get() shouldBe "Hello, World!"
  }
}
