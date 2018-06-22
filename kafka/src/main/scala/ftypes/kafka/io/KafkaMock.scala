package ftypes.kafka.io

import java.util.concurrent.atomic.AtomicLong

import cats.effect.Concurrent
import cats.implicits._
import ftypes.kafka
import ftypes.kafka._
import ftypes.log.Logging
import org.apache.kafka.clients.consumer.{MockConsumer, OffsetResetStrategy}
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.collection.JavaConverters._

case class KafkaMock[F[_]](topics: String*)
                          (implicit F: Concurrent[F], L: Logging[F]) extends Consumer[F] with Producer[F] {

  private val offset = new AtomicLong(0L)
  private val defaultPartition = 0

  private val topicPartition = topics.map { topic =>
    new TopicPartition(topic, defaultPartition)
  }

  private lazy val mockProducer = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
  private lazy val mockConsumer = new MockConsumer[ByteArray, ByteArray](OffsetResetStrategy.EARLIEST)

  private lazy val consumer = new SimpleKafkaConsumer[F](mockConsumer, topics: _*)
  private lazy val producer = new SimpleKafkaProducer[F](mockProducer)

  private def addRecord(record: DefaultProducerRecord): F[Unit] = for {
    r <- F.pure {
      new DefaultConsumerRecord(record.topic(), defaultPartition, offset.getAndIncrement(), record.key(), record.value())
    }
    _ <- F.delay {
      mockConsumer.addRecord(r)
    }
    _ <- L.warn(s"Done $r")
  } yield ()

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
    _  <- F.delay {
      mockConsumer.assign(topicPartition.asJavaCollection)
    }
    _ <- producer.produce(record)
    _ <- F.start(addRecord(record))
  } yield ()

  override def stop: F[Unit] = for {
    _ <- producer.stop
    _ <- consumer.stop
  } yield ()
}
