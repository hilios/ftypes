package ftypes.kafka

import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import ftypes.kafka.Return.{Ack, Error}
import ftypes.kafka.consumers._
import ftypes.kafka.serializers._
import org.scalatest._

class KafkaSpec extends FlatSpec with Matchers with KafkaDsl {

  def createRecord[T](topic: String, message: T)(implicit E: KafkaEncoder[T]) =
    KafkaMessage[IO, T](topic, message)

  "KafkaService" should "return an function that run a KafkaMessage" in {
    val msgA = createRecord("my-topic-a", "Hello, World!")
    val msgB = createRecord("my-topic-b", "Olá, Mundo!")
    
    val helloKafka: KafkaService[IO, IO] = KafkaService[IO] {
      case record @ Topic("my-topic-a") => Ack(record).pure[IO]
      case record @ Topic("my-topic-b") => Ack(record).pure[IO]
    }

    def body(msg: KafkaMessage[IO]): IO[String] = for {
      ret  <- helloKafka(msg)
      body <- ret.message.as[String]
    } yield body

    body(msgA).unsafeRunSync() shouldBe "Hello, World!"
    body(msgB).unsafeRunSync() shouldBe "Olá, Mundo!"
  }

  "KafkaConsumer" should "return an partial function that run a KafkaMessage and can be combined" in {
    val msgEn = createRecord("my-topic-en", "Hello, World!")
    val msgPt = createRecord("my-topic-pt", "Olá, Mundo!")
    val msgEs = createRecord("my-topic-es", "¡Hola, Mundo!")

    val helloKafka: KafkaConsumer[IO] = KafkaConsumer {
      case record @ Topic("my-topic-en") => Ack[IO](record).pure[IO]
    }

    val olaKafka: KafkaConsumer[IO] = KafkaConsumer {
      case record @ Topic("my-topic-pt") => Ack[IO](record).pure[IO]
    }

    val service: KafkaConsumer[IO] = helloKafka <+> olaKafka

    def body(msg: KafkaMessage[IO]): OptionT[IO, String] = for {
      ret  <- service(msg)
      body <- OptionT.liftF(ret.message.as[String])
    } yield body

    body(msgEn).value.unsafeRunSync() shouldBe Some("Hello, World!")
    body(msgPt).value.unsafeRunSync() shouldBe Some("Olá, Mundo!")
    body(msgEs).value.unsafeRunSync() shouldBe None
  }
}
