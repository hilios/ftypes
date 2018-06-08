package ftypes.kafka

import ftypes.kafka

trait Consumer[F[_]] {
  def mountConsumer(consumerDefinition: kafka.KafkaConsumer[F]): F[Unit]
  
  def stop: F[Unit]
}
