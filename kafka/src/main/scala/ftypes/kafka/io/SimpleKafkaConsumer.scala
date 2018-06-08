package ftypes.kafka.io

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.ConcurrentEffect
import cats.implicits._
import ftypes.kafka
import ftypes.kafka.Return.{Ack, Error, NotFound}
import ftypes.kafka._
import ftypes.log.{Logger, Logging}
import org.apache.kafka.clients.consumer.{OffsetAndMetadata, Consumer => KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.annotation.tailrec
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

  @tailrec private def pooling(fn: kafka.KafkaService[F]): F[Unit] = {
    val records = consumer.poll(100).asScala.toList
    val exec = for {
      _ <- {
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
    } yield ()
    // Execute the consumer
    F.toIO(exec).unsafeToFuture()

    if (run.get()) F.pure(())
    else pooling(fn)
  }

  def mountConsumer(consumerDefinition: kafka.KafkaConsumer[F]): F[Unit] = for {
    _  <- L.info(s"Starting consumer for topics ${topics.mkString(", ")}")
    fn <- kafka.seal(consumerDefinition).pure[F]
    _  <- F.delay {
      consumer.subscribe(util.Arrays.asList(topics: _*))
      run.set(true)
    }
    f <- F.start(pooling(fn))
    _ <- f.join
  } yield ()

  def stop: F[Unit] = F.delay(run.set(false)) *> F.delay(consumer.close())
}
