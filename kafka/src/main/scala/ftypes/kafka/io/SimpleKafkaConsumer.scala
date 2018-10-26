package ftypes.kafka.io

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.Concurrent
import cats.implicits._
import ftypes.kafka
import ftypes.kafka.Return.{Ack, Error, NotFound}
import ftypes.kafka._
import ftypes.log.Logging
import org.apache.kafka.clients.consumer.{OffsetAndMetadata, Consumer => KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConverters._

case class SimpleKafkaConsumer[F[_]](consumer: KafkaConsumer[ByteArray, ByteArray], topics: Seq[String])
                                    (implicit F: Concurrent[F], L: Logging[F]) extends Consumer[F] {

  private val run = new AtomicBoolean()

  private def commit(records: List[DefaultConsumerRecord]): F[Unit] = F.async[Unit] { cb =>
    val offsets = records.map { record =>
      (new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()))
    }.toMap
    consumer.commitAsync(offsets.asJava, (_: util.Map[TopicPartition, OffsetAndMetadata], ex: Exception) => {
      if (ex == null) cb(Right(()))
      else cb(Left(ex))
    })
    ()
  } *> {
    if (records.nonEmpty) L.debug(s"Committing offsets ${records.map(_.offset()).mkString(", ")}")
    else F.pure(())
  }

  private def pooling(fn: kafka.KafkaService[F]): F[Unit] = F.flatMap(for {
    rec <- consumer.poll(50).asScala.toList.pure[F]
    _   <- {
      val offsets = rec.map(r => s"${r.topic()}:${r.offset()}").mkString(", ")
      if (rec.nonEmpty) L.debug(s"Consuming records $offsets")
      else F.pure(())
    }
    ret <- rec.map(Record(_)).traverse(fn.apply)
    cmt <- ret.filter {
      case Ack(_)      => true
      case Error(_, _) => true
      case NotFound(_) => false
    }.map(_.record.underlying).pure[F]
    _ <- commit(cmt)
  } yield ()) { _ =>
    if (run.get()) pooling(fn)
    else L.debug(s"Exiting inner consumer loop")
  }

  def mountConsumer(consumerDefinition: kafka.KafkaConsumer[F]): F[Unit] = for {
    _  <- L.info(s"Starting consumer for topics ${topics.mkString(", ")}")
    fn <- kafka.seal(consumerDefinition).pure[F]
    _  <- F.delay {
      if (topics.nonEmpty) consumer.subscribe(util.Arrays.asList(topics: _*))
      run.set(true)
    }
    _  <- F.start(pooling(fn))
  } yield ()

  def stop: F[Unit] = for {
    _ <- L.info("Stopping consumer")
    _ <- F.delay(run.set(false))
    _ <- F.delay(consumer.close())
  } yield ()
}

object SimpleKafkaConsumer {
  def apply[F[_]](consumer: KafkaConsumer[ByteArray, ByteArray], topic: String, topics: String*)
                 (implicit F: Concurrent[F]): SimpleKafkaConsumer[F] =
    new SimpleKafkaConsumer[F](consumer, topic +: topics)
}
