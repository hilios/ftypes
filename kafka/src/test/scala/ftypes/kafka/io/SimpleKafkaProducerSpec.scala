package ftypes.kafka.io

import cats.effect.IO
import ftypes.kafka._
import org.apache.kafka.clients.producer.{MockProducer, ProducerRecord}
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class SimpleKafkaProducerSpec extends FlatSpec with Matchers {
  val mockProducer = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
  val producer = SimpleKafkaProducer[IO](mockProducer)

  "#produce" should "send the message" in {
    val record = new ProducerRecord[ByteArray, ByteArray]("foo-topic", "bar".getBytes())
    producer.produce(record).unsafeRunSync()

    mockProducer.history().asScala.head shouldBe record
  }
}
