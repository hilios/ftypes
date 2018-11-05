package ftypes.log

case class DefaultLogger[F[_]]() extends Logging[F] {
  
  override def log(logger: Logger[F], message: Message): F[Unit] = logger.log(message)
}
