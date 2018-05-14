package ftypes.kafka

import cats.effect.IO
import org.apache.kafka.clients.producer.MockProducer
import org.scalatest._

class KafkaSpec extends FlatSpec with Matchers {

  val mock = new MockProducer[ByteArray, ByteArray]()
//  val service = Kafka[IO](mock)

  it should "provide something really coll"
}
