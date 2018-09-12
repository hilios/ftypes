package ftypes.kamon

import java.time.Instant

import cats.effect.Concurrent
import cats.implicits._
import kamon.trace.{SpanContext, Span => KamonSpan}

case class Span[F[_]](underlying: KamonSpan)(implicit F: Concurrent[F]) {

  def apply[A](fs: KamonSpan => A): F[A] = F.delay(fs(underlying))

  def isEmpty(): F[Boolean] = F.delay(underlying.isEmpty())

  def isLocal(): F[Boolean] = F.delay(underlying.isLocal())
  def nonEmpty(): F[Boolean] = F.delay(underlying.nonEmpty())

  def isRemote(): F[Boolean] = F.delay(underlying.isRemote())

  def context(): F[SpanContext] = F.delay(underlying.context())

  def mark(key: String): F[Unit] = F.delay(underlying.mark(key)).void

  def mark(at: Instant, key: String): F[Unit] = F.delay(underlying.mark(at, key)).void

  def tag(key: String, value: String): F[Unit] = F.delay(underlying.tag(key, value)).void

  def tag(key: String, value: Long): F[Unit] = F.delay(underlying.tag(key, value)).void

  def tag(key: String, value: Boolean): F[Unit] = F.delay(underlying.tag(key, value)).void

  def tagMetric(key: String, value: String): F[Unit] = F.delay(underlying.tagMetric(key, value)).void

  def addError(error: String): F[Unit] = F.delay(underlying.addError(error)).void

  def addError(error: String, throwable: Throwable): F[Unit] = F.delay(underlying.addError(error, throwable)).void

  def setOperationName(name: String): F[Unit] = F.delay(underlying.setOperationName(name)).void

  def enableMetrics(): F[Unit] = F.delay(underlying.enableMetrics()).void

  def disableMetrics(): F[Unit] = F.delay(underlying.disableMetrics()).void

  def finish(at: Instant): F[Unit] = F.delay(underlying.finish(at)).void

  def finish(): F[Unit] = F.delay(underlying.finish()).void
}
