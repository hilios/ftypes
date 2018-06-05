package ftypes.kafka

trait Consumer[F[_]] {
  def mountConsumer(consumer: KafkaConsumer[F]): F[Unit]

  def start: F[Unit]

  def stop: F[Unit]
}
