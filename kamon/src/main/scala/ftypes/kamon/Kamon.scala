package ftypes.kamon

import java.nio.ByteBuffer

import _root_.kamon.context.Context
import _root_.kamon.context.Storage.Scope
import _root_.kamon.metric._
import _root_.kamon.trace.{Span => KamonSpan}
import _root_.kamon.{Tags, Kamon => KamonMetrics}
import cats.effect.{Concurrent, Sync}
import cats.implicits._

case class Kamon[F[_]]()(implicit F: Concurrent[F]) {

  private val noTags = Map.empty[String, String]

  /**
    * Emulates a bracket using @jdegoes suggested implementation.
    * @see https://github.com/typelevel/cats-effect/issues/88#issuecomment-348008156
    */
  private def bracket[A, B](acquire: F[A])(use: A => F[B])(release: A => F[Unit]): F[B] = for {
    a <- acquire
    f <- use(a).attempt
    _ <- release(a)
    o <- f.fold(F.raiseError, F.pure)
  } yield o

  def timer[A](name: String)(fa: F[A])(implicit tags: Tags = noTags): F[A] = bracket(F.delay {
    KamonMetrics.timer(name).refine(tags).start()
  })(_ => fa)(t => F.delay {
    t.stop()
  })

  def trace[A](name: String)(fa: Span[F] => F[A]): F[A] = bracket(F.delay {
    Span(KamonMetrics.buildSpan(name).start())
  })(fa)(_.finish())

  def count(name: String, unit: MeasurementUnit = MeasurementUnit.none)
           (implicit tags: Tags = noTags): F[Unit] = F.delay {
    KamonMetrics.counter(name, unit).refine(tags).increment()
  }.void

  def histogram(name: String, value: Long, unit: MeasurementUnit = MeasurementUnit.none)
               (implicit tags: Tags = noTags): F[Unit] = F.delay {
    KamonMetrics.histogram(name, unit).refine(tags).record(value)
  }.void

  def rangeSampler(name: String, unit: MeasurementUnit = MeasurementUnit.none)
             (fs: RangeSampler => Unit)
             (implicit tags: Tags = noTags): F[Unit] = F.delay {
    fs(KamonMetrics.rangeSampler(name, unit).refine(tags))
  }.void

  def gauge(name: String, unit: MeasurementUnit = MeasurementUnit.none)
           (fg: Gauge => Unit)
           (implicit tags: Tags = noTags): F[Unit] = F.delay {
    fg(KamonMetrics.gauge(name, unit).refine(tags))
  }.void
}

object Kamon {

  def decode[F[_]](bytes: Array[Byte])(implicit F: Sync[F]): F[Context] = F.delay {
    KamonMetrics.contextCodec().Binary.decode(ByteBuffer.wrap(bytes))
  }

  def encode[F[_]](context: Context)(implicit F: Sync[F]): F[Array[Byte]] = F.delay {
    KamonMetrics.contextCodec().Binary.encode(context).array()
  }

  def currentContext[F[_]](implicit F: Sync[F]): F[Context] = F.delay(KamonMetrics.currentContext())

  def currentSpan[F[_]](implicit F: Sync[F]): F[KamonSpan] = F.map(currentContext)(_.get(KamonSpan.ContextKey))

  def scopeFor[F[_]](context: Context, span: KamonSpan)(implicit F: Sync[F]): F[Scope] = F.delay {
    KamonMetrics.storeContext(context.withKey(KamonSpan.ContextKey, span))
  }
}
