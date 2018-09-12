package ftypes.kafka.io

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import cats.effect.IO
import ftypes.kafka._
import ftypes.log.ConsoleLogger
import org.apache.kafka.clients.consumer.{ConsumerRecord, MockConsumer, OffsetResetStrategy}
import org.apache.kafka.common.TopicPartition
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class SimpleKafkaConsumerSpec extends FlatSpec with Matchers with Eventually with KafkaDsl {
  implicit val L = ConsoleLogger[IO]

  val lastMessage = new AtomicReference[String]()

  val offset = new AtomicLong(0L)
  val defaultPartition = 0
  val topic = "test-topic"

  val topicPartition = Seq(
    new TopicPartition(topic, defaultPartition)
  )

  val mockConsumer = new MockConsumer[ByteArray, ByteArray](OffsetResetStrategy.EARLIEST)
  mockConsumer.assign(topicPartition.asJavaCollection)
  mockConsumer.updateBeginningOffsets(topicPartition.map((_, new java.lang.Long(0L))).toMap.asJava)
  mockConsumer.seekToBeginning(topicPartition.asJava)

  val fn = KafkaConsumer[IO] {
    case msg @ Topic("test-topic") => for {
      _ <- L.info(s"Consuming message ${msg.offset} from ${msg.topic}")
      s <- msg.as[String]
      _ <- IO {
        lastMessage.set(s)
      }
    } yield ()
  }

  val consumer = SimpleKafkaConsumer[IO](mockConsumer, Seq.empty[String])

  "#mountConsumer" should "call the consumer for each new message" in {
    val record = new ConsumerRecord[ByteArray, ByteArray](topic, defaultPartition, offset.getAndIncrement(), null, "Hello, World!".getBytes())

    val test: IO[Unit] = for {
      _ <- IO(mockConsumer.addRecord(record))
      _ <- consumer.mountConsumer(fn)
      _ <- consumer.stop
    } yield ()

    test.unsafeRunSync()
    lastMessage.get() shouldBe "Hello, World!"
  }
}
