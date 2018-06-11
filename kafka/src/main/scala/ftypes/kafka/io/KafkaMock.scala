package ftypes.kafka.io

import java.util.concurrent.atomic.AtomicLong

import cats.effect.ConcurrentEffect
import cats.implicits._
import ftypes.kafka
import ftypes.kafka._
import ftypes.log.{Logger, Logging}
import org.apache.kafka.clients.consumer.{MockConsumer, OffsetResetStrategy}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.collection.JavaConverters._

case class KafkaMock[F[_]](topics: String*)
                          (implicit F: ConcurrentEffect[F], L: Logging[F]) extends Consumer[F] with Producer[F] {

  implicit val logger = Logger

  private val offset = new AtomicLong(0L)

  private val topicPartition = topics.map { topic =>
    new TopicPartition(topic, 0)
  }
  
  private val mockProducer = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
  private val mockConsumer = new MockConsumer[ByteArray, ByteArray](OffsetResetStrategy.EARLIEST)

  private val consumer = new SimpleKafkaConsumer[F](mockConsumer, topics: _*)
  private val producer = new SimpleKafkaProducer[F](mockProducer)

  override def mountConsumer(consumerDefinition: KafkaConsumer[F]): F[Unit] = for {
    _  <- L.info(s"Starting consumer for topics ${topics.mkString(", ")}")
    fn <- kafka.seal(consumerDefinition).pure[F]
    _  <- F.delay {
      mockConsumer.assign(topicPartition.asJavaCollection)
      mockConsumer.updateBeginningOffsets(topicPartition.map((_, new java.lang.Long(0L))).toMap.asJava)
      mockConsumer.seekToBeginning(topicPartition.asJava)
    }
    _  <- consumer.start(fn)
  } yield ()

  override def produce(record: DefaultProducerRecord): F[Unit] = for {
    _ <- producer.produce(record)
    _ <- F.delay {
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

