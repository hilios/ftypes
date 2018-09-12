package ftypes.kafka

import _root_.io.circe.literal._
import _root_.io.circe.{Decoder, Encoder}
import cats.effect.IO
import ftypes.kafka.circe._
import ftypes.kafka.io.SimpleKafkaProducer
import ftypes.log.ConsoleLogger
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.scalatest._

import scala.collection.JavaConverters._

class KafkaCirceSpec extends FlatSpec with Matchers {
  implicit val logger = ConsoleLogger[IO]

  val mockProducer = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
  val kafka = SimpleKafkaProducer[IO](mockProducer)

  case class TestMessage(a: String, b: Boolean, c: List[Int])

  val message = TestMessage("foo", b = false, List(1, 2, 3))
  val jsonMessage = json"""
      {
        "a": "foo",
        "b": false,
        "c": [1,2,3]
      }
    """.noSpaces

  def lastMessage: Option[String] = mockProducer.history().asScala.headOption.map { record =>
    record.value().map(_.toChar).mkString
  }

  implicit val messageEncoder: Encoder[TestMessage] =
    Encoder.forProduct3("a", "b", "c")(m => (m.a, m.b, m.c))

  implicit val messageDecoder: Decoder[TestMessage] =
    Decoder.forProduct3("a", "b", "c")(TestMessage.apply)

  it should "derive the encoder if there is an implicit circe encoder" in {
    kafka.produce("test" -> message).unsafeRunSync()
    lastMessage shouldBe Some(jsonMessage)
  }

  it should "derive the decoder if there is an implicit circe decoder" in {
    val record = Record[IO](new DefaultConsumerRecord("test", 0, 0L, Array.empty[Byte], jsonMessage.getBytes()))
    record.as[TestMessage].unsafeRunSync() shouldBe message
  }
}
