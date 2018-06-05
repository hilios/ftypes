package ftypes

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import ftypes.kafka.Return.{Ack, Error, NotFound}
import org.apache.kafka.clients.consumer.{ConsumerRecord, Consumer => ApacheConsumer}
import org.apache.kafka.clients.producer.{ProducerRecord, Producer => ApacheProducer}

package object kafka {
  type ByteArray = Array[Byte]
  type SingleMessage[T] = (String, T)
  type DefaultConsumer = ApacheConsumer[ByteArray, ByteArray]
  type DefaultProducer = ApacheProducer[ByteArray, ByteArray]
  type DefaultConsumerRecord = ConsumerRecord[ByteArray, ByteArray]
  type DefaultProducerRecord = ProducerRecord[ByteArray, ByteArray]

  type KafkaEffect[F[_], G[_]] = Kleisli[F, Record[G], Return[G]]
  type KafkaService[F[_]] = KafkaEffect[F, F]
  type KafkaConsumer[F[_]] = Kleisli[OptionT[F, ?], Record[F], Unit]

  def KafkaService[F[_]](f: Record[F] => F[Return[F]]): KafkaService[F] = Kleisli(f)

  def seal[F[_]](service: KafkaConsumer[F])(implicit F: Sync[F]): KafkaService[F] = Kleisli(record => {
    lazy val notFound: Return[F] = NotFound(record)
    F.recover(service(record).map(_ => Ack(record)).getOrElse(notFound)) {
      case ex: Throwable => Error(record, ex)
    }
  })
}
