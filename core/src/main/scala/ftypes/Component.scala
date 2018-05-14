package ftypes

import cats.effect.Async
import cats.implicits._

trait Component[F[_]] {
  def start: F[Unit]

  def stop: F[Unit]
}

object Component {
  def start[F[_]](components: Component[F]*)(implicit F: Async[F]): F[Unit] =
    components.toList.traverse(_.start).map(_ => ())

  def stop[F[_]](components: Component[F]*)(implicit F: Async[F]): F[Unit] =
    components.toList.traverse(_.stop).map(_ => ())
}
