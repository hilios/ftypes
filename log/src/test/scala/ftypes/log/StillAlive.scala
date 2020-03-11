package ftypes.log

import cats.effect.Sync
import cats.implicits._

class StillAlive[F[_]](implicit F: Sync[F], L: Log[F]) {

  def log: F[Unit] =
    for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
}

object StillAlive {
  def apply[F[_]](implicit F: Sync[F], L: Log[F]): StillAlive[F] = new StillAlive[F]
}
