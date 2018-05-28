package ftypes.kafka

import cats.effect.IO
import ftypes.kafka.circe._
import ftypes.kafka.producer.KafkaProducer
import ftypes.test.logging.PrintLog
import io.circe.{Decoder, Encoder}
import io.circe.literal._
import org.apache.kafka.clients.consumer.{MockConsumer, OffsetResetStrategy}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.scalatest._

import scala.collection.JavaConverters._

class KafkaCirceSpec extends FlatSpec with Matchers {
  implicit val logger = PrintLog[IO]

  val mockConsumer = new MockConsumer[ByteArray, ByteArray](OffsetResetStrategy.EARLIEST)
  val mockProducer = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
  val kafka = KafkaProducer[IO](mockProducer)

  case class Message(a: String, b: Boolean, c: List[Int])

  def lastMessage: Option[String] = mockProducer.history().asScala.headOption.map { record =>
    record.value().map(_.toChar).mkString
  }

  implicit val messageEncoder: Encoder[Message] =
    Encoder.forProduct3("a", "b", "c")(m => (m.a, m.b, m.c))
  
  implicit val messageDecoder: Decoder[Message] =
    Decoder.forProduct3("a", "b", "c")(Message.apply)

  it should "derive the encoder if there is a implicit circe encoder" in {
    kafka.produce("test" -> Message("foo", b = false, List(1, 2, 3))).unsafeRunSync()
    lastMessage shouldBe Some(json"""
      {
        "a": "foo",
        "b": false,
        "c": [1,2,3]
      }
    """.noSpaces)
  }
}
