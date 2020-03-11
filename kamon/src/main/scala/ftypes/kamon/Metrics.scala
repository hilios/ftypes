package ftypes.kamon

import _root_.kamon.Kamon
import _root_.kamon.metric.{DynamicRange, MeasurementUnit, RangeSampler}
import _root_.kamon.tag.TagSet
import _root_.kamon.trace.Span
import cats.effect.{ExitCase, Sync}
import cats.implicits._

final class Metrics[F[_]] private (implicit F: Sync[F]) {
  type Tag = (String, Any)

  def counter(name: String, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.counter(name).withTags(TagSet.from(tags.toMap)).increment()
    }.void

  def counter(name: String, times: Long, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.counter(name).withTags(TagSet.from(tags.toMap)).increment(times)
    }.void

  def gauge(name: String, value: Double, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.gauge(name).withTags(TagSet.from(tags.toMap)).update(value)
    }.void

  def gauge(name: String, value: Double, unit: MeasurementUnit, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.gauge(name, unit).withTags(TagSet.from(tags.toMap)).update(value)
    }.void

  def histogram(name: String, value: Long, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.histogram(name).withTags(TagSet.from(tags.toMap)).record(value)
    }.void

  def histogram(name: String, value: Long, unit: MeasurementUnit, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.histogram(name, unit).withTags(TagSet.from(tags.toMap)).record(value)
    }.void

  def histogram(name: String, value: Long, unit: MeasurementUnit, dynamicRange: DynamicRange, tags: Tag*): F[Unit] =
    F.delay {
      Kamon.histogram(name, unit, dynamicRange).withTags(TagSet.from(tags.toMap)).record(value)
    }.void

  def timer[A](name: String, tags: Tag*)(fa: F[A]): F[A] =
    F.bracket(F.delay {
      Kamon.timer(name).withTags(TagSet.from(tags.toMap)).start()
    })(_ => fa)(timer => F.delay(timer.stop()))

  def timer[A](name: String, dynamicRange: DynamicRange, tags: Tag*)(fa: F[A]): F[A] =
    F.bracket(F.delay {
      Kamon.timer(name, dynamicRange).withTags(TagSet.from(tags.toMap)).start()
    })(_ => fa)(timer => F.delay(timer.stop()))

  def rangeSampler[A](name: String, tags: Tag*)(fa: RangeSampler => F[A]): F[Unit] =
    F.delay {
        Kamon.rangeSampler(name).withTags(TagSet.from(tags.toMap))
      }
      .flatMap(fa)
      .void

  def rangeSampler[A](name: String, unit: MeasurementUnit, tags: Tag*)(
    fa: _root_.kamon.metric.RangeSampler => F[A]): F[Unit] =
    F.delay {
        Kamon.rangeSampler(name, unit).withTags(TagSet.from(tags.toMap))
      }
      .flatMap(fa)
      .void

  def rangeSampler[A](name: String, unit: MeasurementUnit, dynamicRange: DynamicRange, tags: Tag*)(
    fa: _root_.kamon.metric.RangeSampler => F[A]): F[Unit] =
    F.delay {
        Kamon.rangeSampler(name, unit, dynamicRange).withTags(TagSet.from(tags.toMap))
      }
      .flatMap(fa)
      .void
}

object Metrics {
  implicit def apply[F[_]: Sync]: Metrics[F] = new Metrics[F]
}
