package ftypes.kafka.io

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.ConcurrentEffect
import cats.implicits._
import ftypes.kafka
import ftypes.kafka.Return.{Ack, Error, NotFound}
import ftypes.kafka._
import ftypes.log.{Logger, Logging}
import org.apache.kafka.clients.consumer.{ConsumerRebalanceListener, OffsetAndMetadata, Consumer => KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConverters._

case class SimpleKafkaConsumer[F[_]](consumer: KafkaConsumer[ByteArray, ByteArray], topics: String*)
                                    (implicit F: ConcurrentEffect[F], L: Logging[F]) extends Consumer[F] {

  implicit val logger = Logger

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

  private def consume(fn: kafka.KafkaService[F], record: DefaultConsumerRecord): F[Return[F]] = for {
    msg    <- Record(record).pure[F]
    result <- fn.apply(msg)
  } yield result

  private def pooling(fn: kafka.KafkaService[F]): F[Unit] = F.flatMap(for {
    records <- consumer.poll(1).asScala.toList.pure[F]
    _       <- {
      val offsets = records.map(r => s"${r.topic()}(${r.offset()})").mkString(", ")
      if (records.nonEmpty) L.debug(s"Consuming records $offsets")
      else F.pure(())
    }
    ret <- records.traverse(consume(fn, _))
    rec <- F.pure {
      ret.filter {
        case Ack(_)      => true
        case Error(_, _) => true
        case NotFound(_) => false
      }.map(_.record.underlying)
    }
    _ <- commit(rec)
  } yield ()) { _ =>
    if (run.get()) pooling(fn)
    else L.debug(s"Exiting inner consumer loop")
  }

  def mountConsumer(consumerDefinition: kafka.KafkaConsumer[F]): F[Unit] = for {
    _  <- L.info(s"Starting consumer for topics ${topics.mkString(", ")}")
    fn <- kafka.seal(consumerDefinition).pure[F]
    _  <- F.delay {
      consumer.subscribe(util.Arrays.asList(topics: _*))
    }
    _  <- start(fn)
  } yield ()

  def start(fn: kafka.KafkaService[F]): F[Unit] = for {
    _     <- F.delay(run.set(true))
    fiber <- F.start(pooling(fn))
    _     <- fiber.join
  } yield ()

  def stop: F[Unit] = for {
    _ <- L.info("Stopping consumer")
    _ <- F.delay(run.set(false))
    _ <- F.delay(consumer.close())
  } yield ()
}
