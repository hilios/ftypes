package ftypes.kafka

import cats.effect.IO
import cats.implicits._
import ftypes.kafka.Return.{Ack, Error, NotFound}
import ftypes.kafka.serializers._
import org.scalatest._

class KafkaConsumerSpec extends FlatSpec with Matchers with Inside with KafkaDsl {

  def createRecord[T](topic: String, message: T)(implicit E: KafkaEncoder[T]): Record[IO] =
    Record[IO, T](topic, message)

  it should "return a Kleisli from a partial function that may run a KafkaMessage or not" in {
    val msgEn = createRecord("my-topic-en", "Hello, World!")
    val msgPt = createRecord("my-topic-pt", "Olá, Mundo!")
    val msgEs = createRecord("my-topic-es", "¡Hola, Mundo!")

    val helloKafka: KafkaConsumer[IO] = KafkaConsumer {
      case record @ Topic("my-topic-en") => for {
        _ <- record.as[String]
      } yield ()
    }

    val olaKafka: KafkaConsumer[IO] = KafkaConsumer {
      case record @ Topic("my-topic-pt") => for {
        _ <- record.as[String]
      } yield ()
    }

    val service: KafkaConsumer[IO] = helloKafka <+> olaKafka

    service.apply(msgEn).value.unsafeRunSync() shouldBe Some(())
    service.apply(msgPt).value.unsafeRunSync() shouldBe Some(())
    service.apply(msgEs).value.unsafeRunSync() shouldBe None
  }

  "KafkaService" should "return an Kleisli that run a KafkaMessage" in {
    val msgA = createRecord("my-topic-a", "Hello, World!")
    val msgB = createRecord("my-topic-b", "Olá, Mundo!")

    val helloKafka = KafkaService[IO] {
      case record @ Topic("my-topic-a") => Ack(record).pure[IO]
      case record @ Topic("my-topic-b") => Ack(record).pure[IO]
    }

    def body(msg: Record[IO]): IO[String] = for {
      ret  <- helloKafka(msg)
      body <- ret.record.as[String]
    } yield body

    body(msgA).unsafeRunSync() shouldBe "Hello, World!"
    body(msgB).unsafeRunSync() shouldBe "Olá, Mundo!"
  }

  ".seal" should "return a KafkaService from a KafkaConsumer" in {
    val msgEn = createRecord("my-topic-en", "Hello, World!")
    val msgPt = createRecord("my-topic-pt", "Olá, Mundo!")
    val msgEs = createRecord("my-topic-es", "¡Hola, Mundo!")

    val service: KafkaConsumer[IO] = KafkaConsumer {
      case Topic("my-topic-en") => IO.pure(())
      case Topic("my-topic-pt") => IO.pure(())
    }

    val consumer = service.seal

    consumer(msgEn).unsafeRunSync() shouldBe Ack(msgEn)
    consumer(msgPt).unsafeRunSync() shouldBe Ack(msgPt)
    consumer(msgEs).unsafeRunSync() shouldBe NotFound(msgEs)
  }


  it should "lift any exception into the Error data type" in {
    val msg = createRecord("boom", "Hello, World!")
    val ex  = new Exception("Boom!")

    val consumer: KafkaConsumer[IO] = KafkaConsumer[IO] {
      case Topic("boom") => IO.raiseError(ex)
    }

    consumer.seal(msg).unsafeRunSync() shouldBe Error(msg, ex)
  }

  it should "return a NotFound data type if cannot find a consumer to execute" in {
    val msg = createRecord("not-found", "Hello, World!")

    val consumer: KafkaConsumer[IO] = KafkaConsumer[IO] {
      case Topic("my-topic") => IO.pure(())
    }

    consumer.seal(msg).unsafeRunSync() shouldBe NotFound(msg)
  }
}
