package ftypes.kafka.io

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.ConcurrentEffect
import cats.implicits._
import ftypes.kafka
import ftypes.kafka.Return.{Ack, Error, NotFound}
import ftypes.kafka._
import org.apache.kafka.clients.consumer.{OffsetAndMetadata, Consumer => KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConverters._

case class SimpleKafkaConsumer[F[_]](topics: Seq[String], consumer: KafkaConsumer[ByteArray, ByteArray])
                                    (implicit F: ConcurrentEffect[F]) extends Consumer[F] {

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
  }

  private def consume(fn: kafka.KafkaService[F], record: DefaultConsumerRecord): F[Return[F]] = for {
    msg    <- Record(record).pure[F]
    result <- fn.apply(msg)
  } yield result

  private def pooling(fn: kafka.KafkaService[F]): F[Unit] = F.delay {
    while (run.get()) {
      val records = consumer.poll(100).asScala.toList
      val exec = for {
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
    }
  }

  def mountConsumer(consumerDefinition: kafka.KafkaConsumer[F]): F[Unit] = for {
    fn <- kafka.seal(consumerDefinition).pure[F]
    _  <- F.delay {
      consumer.subscribe(util.Arrays.asList(topics: _*))
      run.set(true)
    }
    f <- F.start(pooling(fn))
    _ <- f.join
  } yield ()

  def stop: F[Unit] = F.delay(run.set(false))

}
