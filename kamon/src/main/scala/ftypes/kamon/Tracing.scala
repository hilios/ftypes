package ftypes.kamon

import cats.effect.{ExitCase, Sync}
import cats.implicits._
import kamon.Kamon
import kamon.trace.Span

class Tracing[F[_]] private (implicit F: Sync[F]) {

  def delayedSpan[A](name: String, component: String)(fa: Span.Delayed => F[A]): F[A] =
    F.bracketCase(F.delay {
      Kamon.internalSpanBuilder(name, component).delay()
    })(fa) {
      case (span, ExitCase.Completed) => F.delay(span.finish())
      case (span, ExitCase.Error(ex)) => F.delay(span.fail(ex)) >> F.delay(span.finish())
      case (span, ExitCase.Canceled)  => F.delay(span.fail(errorMessage = "canceled")) >> F.delay(span.finish())
    }

  def buildSpan[A](name: String, component: String)(fa: F[A]): F[A] =
    delayedSpan(name, component) { span =>
      F.delay(span.start()).flatMap(_ => fa)
    }
}
