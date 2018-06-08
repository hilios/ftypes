package ftypes.kafka.io

import java.util.concurrent.atomic.AtomicLong

import cats.effect.ConcurrentEffect
import cats.implicits._
import ftypes.kafka._
import ftypes.log.Logging
import org.apache.kafka.clients.consumer.{MockConsumer, OffsetResetStrategy}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.ByteArraySerializer

case class KafkaMock[F[_]](topics: String*)
                          (implicit F: ConcurrentEffect[F], L: Logging[F]) extends Consumer[F] with Producer[F] {

  private val offset = new AtomicLong(0L)
  
  private val mockProducer = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
  private val mockConsumer = new MockConsumer[ByteArray, ByteArray](OffsetResetStrategy.EARLIEST)

  private val consumer = new SimpleKafkaConsumer[F](mockConsumer, topics: _*)
  private val producer = new SimpleKafkaProducer[F](mockProducer)

  override def mountConsumer(consumerDefinition: KafkaConsumer[F]): F[Unit] =
    consumer.mountConsumer(consumerDefinition)

  override def produce(record: DefaultProducerRecord): F[Unit] = for {
    _ <- producer.produce(record)
    _ <- F.delay {
      println(mockConsumer.partitionsFor(topics.head))
      val r = new DefaultConsumerRecord(record.topic(), 0, offset.getAndIncrement(),
        record.key(), record.value())
      mockConsumer.addRecord(r)
    }
  } yield ()

  override def stop: F[Unit] = for {
    _ <- producer.stop
    _ <- consumer.stop
  } yield ()
}

