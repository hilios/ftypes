package ftypes.kamon

import java.time.Instant

import cats.effect.Concurrent
import cats.implicits._
import kamon.trace.{SpanContext, Span => KamonSpan}

case class Span[F[_]](span: KamonSpan)(implicit F: Concurrent[F]) {

  private def fireAndForget[A](thunk: => A): F[Unit] = F.start(F.delay(thunk)).void

  def isEmpty(): F[Boolean] = F.delay(span.isEmpty())
  def isLocal(): F[Boolean] = F.delay(span.isLocal())

  def nonEmpty(): F[Boolean] = F.delay(span.nonEmpty())
  def isRemote(): F[Boolean] = F.delay(span.isRemote())

  def context(): F[SpanContext] = F.delay(span.context())

  def mark(key: String): F[Unit] = fireAndForget(span.mark(key))

  def mark(at: Instant, key: String): F[Unit] = fireAndForget(span.mark(at, key))

  def tag(key: String, value: String): F[Unit] = fireAndForget(span.tag(key, value))

  def tag(key: String, value: Long): F[Unit] = fireAndForget(span.tag(key, value))

  def tag(key: String, value: Boolean): F[Unit] = fireAndForget(span.tag(key, value))

  def tagMetric(key: String, value: String): F[Unit] = fireAndForget(span.tagMetric(key, value))

  def addError(error: String): F[Unit] = fireAndForget(span.addError(error))

  def addError(error: String, throwable: Throwable): F[Unit] = fireAndForget(span.addError(error, throwable))

  def setOperationName(name: String): F[Unit] = fireAndForget(span.setOperationName(name))

  def enableMetrics(): F[Unit] = fireAndForget(span.enableMetrics())

  def disableMetrics(): F[Unit] = fireAndForget(span.disableMetrics())

  def finish(at: Instant): F[Unit] = fireAndForget(span.finish(at))

  def finish(): F[Unit] = fireAndForget(span.finish())
}
