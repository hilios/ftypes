package ftypes.kafka

trait KafkaDecoder[T] {
  def decode(value: ByteArray): T
}
