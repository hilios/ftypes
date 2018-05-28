package ftypes.kafka

import cats.effect.IO
import cats.implicits._
import ftypes.kafka.Return.{Ack, Error}
import ftypes.kafka.consumers._
import ftypes.kafka.serializers._
import org.scalatest._

class KafkaSpec extends FlatSpec with Matchers with KafkaDsl {

  def createRecord[T](topic: String, message: T)(implicit E: KafkaEncoder[T]): KafkaMessage[IO] =
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
      case Topic("my-topic-en") => IO.pure(())
    }

    val olaKafka: KafkaConsumer[IO] = KafkaConsumer {
      case Topic("my-topic-pt") => IO.pure(())
    }

    val service: KafkaConsumer[IO] = helloKafka <+> olaKafka

    def body(msg: KafkaMessage[IO]): IO[String] = for {
      out  <- service.compile(msg)
      body <- out match {
        case Ack(m)      => m.as[String]
        case Error(_, e) => throw e
      }
    } yield body

    body(msgEn).attempt.unsafeRunSync() shouldBe Right("Hello, World!")
    body(msgPt).attempt.unsafeRunSync() shouldBe Right("Olá, Mundo!")
    body(msgEs).attempt.unsafeRunSync() shouldBe a[Left[RuntimeException, _]]
  }
}
