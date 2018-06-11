package ftypes.kafka.io

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import ftypes.kafka.{KafkaConsumer, KafkaDsl, SerializerImplicits}
import ftypes.log.Logger
import ftypes.log.utils.PrintLog
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class KafkaSpec extends FlatSpec with Matchers with SerializerImplicits with BeforeAndAfterAll with KafkaDsl {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(3))

  val lastMessage = new AtomicReference[String]()
  val topic = "test"

  implicit val logger = Logger
  implicit val L = PrintLog[IO]

  val fn = KafkaConsumer[IO] {
    case msg @ Topic("test") => for {
      _ <- L.info(s"Consuming message ${msg.offset} from ${msg.topic}")
      s <- msg.as[String]
      _ <- IO {
        lastMessage.set(s)
      }
    } yield ()
  }

  val kafka = KafkaMock[IO](topic)

  override def beforeAll(): Unit = {
    kafka.mountConsumer(fn).unsafeToFuture() recover {
      case ex: Throwable => println(ex)
    }
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    kafka.stop.unsafeRunSync()
    super.afterAll()
  }

  it should "produce and consume a message" in {
    val test = for {
      _ <- kafka.produce(topic, "Hello, World!")
      _ <- IO(Thread.sleep(150))
    } yield ()
    test.unsafeRunSync()
    lastMessage.get() shouldBe "Hello, World!"
  }
}
