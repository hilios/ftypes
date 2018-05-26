package ftypes.kafka

object serializers extends SerializerImplicits {
  trait KafkaDecoder[T] {
    def decode(value: ByteArray): T
  }

  trait KafkaEncoder[T] {
    def encode(value: T): ByteArray
  }
}
