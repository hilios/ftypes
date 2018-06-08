package ftypes.kafka

import org.scalatest.{FlatSpec, Matchers}

class SerializersImplicitsSpec extends FlatSpec with Matchers with SerializerImplicits {

  it should "should encode/decode a Float" in {
    (floatKafkaEncoder.encode _ andThen floatKafkaDecoder.decode)(1.0f) shouldBe 1.0f
  }

  it should "should encode/decode a Int" in {
    (intKafkaEncoder.encode _ andThen intKafkaDecoder.decode)(1) shouldBe 1
  }

  it should "should encode/decode a Double" in {
    (doubleKafkaEncoder.encode _ andThen doubleKafkaDecoder.decode)(1.0f) shouldBe 1.0f
  }

  it should "should encode/decode a Long" in {
    (longKafkaEncoder.encode _ andThen longKafkaDecoder.decode)(1L) shouldBe 1L
  }

  it should "should encode/decode a Short" in {
    (shortKafkaEncoder.encode _ andThen shortKafkaDecoder.decode)(1.toShort) shouldBe 1.toShort
  }

  it should "should encode/decode a String" in {
    (stringKafkaEncoder.encode _ andThen stringKafkaDecoder.decode)("Hello, World!") shouldBe "Hello, World!"
    (stringKafkaEncoder.encode _ andThen stringKafkaDecoder.decode)("Mátyás") shouldBe "Mátyás"
    (stringKafkaEncoder.encode _ andThen stringKafkaDecoder.decode)("你好") shouldBe "你好"
  }

}
