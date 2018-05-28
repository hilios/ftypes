package ftypes.kafka

import cats.effect.IO
import cats.implicits._
import ftypes.kafka.consumer.Return.{Ack, Error}
import ftypes.kafka.consumer.{KafkaConsumer, KafkaDsl, KafkaService, Message}
import ftypes.kafka.serializers._
import org.scalatest._

class KafkaConsumerSpec extends FlatSpec with Matchers with Inside with KafkaDsl {

  def createRecord[T](topic: String, message: T)(implicit E: KafkaEncoder[T]): Message[IO] =
    Message[IO, T](topic, message)

  it should "return an Kleisli that run a KafkaMessage" in {
    val msgA = createRecord("my-topic-a", "Hello, World!")
    val msgB = createRecord("my-topic-b", "Olá, Mundo!")
    
    val helloKafka = KafkaConsumer[IO] {
      case record @ Topic("my-topic-a") => Ack(record).pure[IO]
      case record @ Topic("my-topic-b") => Ack(record).pure[IO]
    }

    def body(msg: Message[IO]): IO[String] = for {
      ret  <- helloKafka(msg)
      body <- ret.message.as[String]
    } yield body

    body(msgA).unsafeRunSync() shouldBe "Hello, World!"
    body(msgB).unsafeRunSync() shouldBe "Olá, Mundo!"
  }

  behavior of "KafkaService"

  it should "return a Kleisli from a partial function that may return some consumer" in {
    val msgEn = createRecord("my-topic-en", "Hello, World!")
    val msgPt = createRecord("my-topic-pt", "Olá, Mundo!")
    val msgEs = createRecord("my-topic-es", "¡Hola, Mundo!")


    val helloKafka: KafkaService[IO] = KafkaService {
      case record @ Topic("my-topic-en") => IO.suspend(record.as[String])
    }

    val olaKafka: KafkaService[IO] = KafkaService {
      case record @ Topic("my-topic-pt") => IO.suspend(record.as[String])
    }

    val service: KafkaService[IO] = helloKafka <+> olaKafka

    def body(msg: Message[IO]): IO[Option[String]] =
      service(msg).map(_.asInstanceOf[String]).value

    body(msgEn).unsafeRunSync() shouldBe Some("Hello, World!")
    body(msgPt).unsafeRunSync() shouldBe Some("Olá, Mundo!")
    body(msgEs).unsafeRunSync() shouldBe None
  }

  ".compile" should "return a KafkaConsumer from a KafkaService" in {
    val msgEn = createRecord("my-topic-en", "Hello, World!")
    val msgPt = createRecord("my-topic-pt", "Olá, Mundo!")
    val msgEs = createRecord("my-topic-es", "¡Hola, Mundo!")

    val service: KafkaService[IO] = KafkaService {
      case Topic("my-topic-en") => IO.pure(())
      case Topic("my-topic-pt") => IO.pure(())
    }

    val consumer = service.compile

    consumer(msgEn).unsafeRunSync() shouldBe Ack(msgEn)
    consumer(msgPt).unsafeRunSync() shouldBe Ack(msgPt)
    inside(consumer(msgEs).unsafeRunSync()) { case Error(message, ex) =>
      message shouldBe msgEs
      ex shouldBe a[RuntimeException]
    }
  }


  it should "lift any exception into the Error data type" in {
    val msg = createRecord("my-topic-a", "Hello, World!")
    val ex  = new Exception("Boom!")

    val consumer: KafkaService[IO] = KafkaService[IO] {
      case Topic("my-topic-a") => IO.raiseError(ex)
    }

    consumer.compile(msg).unsafeRunSync() shouldBe Error(msg, ex)
  }

  it should "return an RuntimeException wrapped into the Error data type if cannot find the route" in {
    val msg = createRecord("my-topic", "Hello, World!")

    val consumer: KafkaService[IO] = KafkaService[IO] {
      case Topic("boom") => IO.pure(())
    }

    consumer.compile(msg).flatMap {
      case Ack(message) => message.as[String].map(Right(_))
      case Error(_, ex) => IO.pure(Left(ex.getMessage))
    }.unsafeRunSync() shouldBe Left("Consumer for topic my-topic was not found")
  }
}
