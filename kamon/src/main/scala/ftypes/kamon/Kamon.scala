package ftypes.kamon

import cats.effect.Concurrent
import cats.implicits._
import kamon.metric._
import kamon.{Tags, Kamon => KamonMetrics}

case class Kamon[F[_]]()(implicit F: Concurrent[F]) {

  private val noTags = Map.empty[String, String]

  private def fireAndForget(thunk: => Unit): F[Unit] = F.start(F.delay(thunk)).void

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
           (implicit tags: Tags = noTags): F[Unit] = fireAndForget {
    KamonMetrics.counter(name, unit).refine(tags).increment()
  }

  def histogram(name: String, value: Long, unit: MeasurementUnit = MeasurementUnit.none)
               (implicit tags: Tags = noTags): F[Unit] = fireAndForget {
    KamonMetrics.histogram(name, unit).refine(tags).record(value)
  }

  def rangeSampler(name: String, unit: MeasurementUnit = MeasurementUnit.none)
             (fs: RangeSampler => Unit)
             (implicit tags: Tags = noTags): F[Unit] = fireAndForget {
    fs(KamonMetrics.rangeSampler(name, unit).refine(tags))
  }

  def gauge(name: String, unit: MeasurementUnit = MeasurementUnit.none)
           (fg: Gauge => Unit)
           (implicit tags: Tags = noTags): F[Unit] = fireAndForget {
    fg(KamonMetrics.gauge(name, unit).refine(tags))
  }
}
