package ftypes.kafka

import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import ftypes.kafka.Return.Ack
import ftypes.kafka.consumers._
import ftypes.kafka.serializers._
import org.scalatest._

class KafkaSpec extends FlatSpec with Matchers with KafkaDsl {

  def createRecord[T](topic: String, message: T)(implicit E: KafkaEncoder[T]) =
    new DefaultConsumerRecord(topic, 0, 0L, Array.empty[Byte], E.encode(message))

  it should "provide something really cool" in {
    val msgA = createRecord("my-topic-a", "Hello, World!")
    val msgB = createRecord("my-topic-b", "Olá, Mundo!")
    
    val helloKafka: KafkaService[IO] = KafkaService[IO] {
      case record @ Topic("my-topic-a") => Ack(record).pure[IO]
      case record @ Topic("my-topic-b") => Ack(record).pure[IO]
    }

    def body(msg: DefaultConsumerRecord): IO[String] = for {
      ret  <- helloKafka(msg)
      body <- IO(ret.record.as[String])
    } yield body

    body(msgA).unsafeRunSync() shouldBe "Hello, World!"
    body(msgB).unsafeRunSync() shouldBe "Olá, Mundo!"
  }

  it should "combine" in {
    val msgEn = createRecord("my-topic-en", "Hello, World!")
    val msgPt = createRecord("my-topic-pt", "Olá, Mundo!")

    val helloKafka: KafkaConsumer[IO] = KafkaConsumer {
      case record @ Topic("my-topic-en") => IO(Return(record))
    }

    val olaKafka: KafkaConsumer[IO] = KafkaConsumer {
      case record @ Topic("my-topic-pt") => IO(Return(record))
    }

    val service: KafkaConsumer[IO] = helloKafka <+> olaKafka

    def body(msg: DefaultConsumerRecord): OptionT[IO, String] = for {
      ret  <- service(msg)
      body <- OptionT.liftF(IO(ret.record.as[String]))
    } yield body

    body(msgEn).value.unsafeRunSync() shouldBe Some("Hello, World!")
    body(msgPt).value.unsafeRunSync() shouldBe Some("Olá, Mundo!")
  }
}
