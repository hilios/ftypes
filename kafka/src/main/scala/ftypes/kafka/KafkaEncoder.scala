package ftypes.kafka

trait KafkaEncoder[T] {
  def encode(value: T): ByteArray
}
