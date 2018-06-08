package ftypes.kafka

import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}

class KafkaDslSpec extends FlatSpec with Matchers with KafkaDsl {

  behavior of "Topic"

  it should "extract the topic from a record" in {
    val Topic(topic) = Record[IO, String]("foo", "bar")
    topic shouldBe "foo"
  }
}
