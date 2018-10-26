package ftypes.log

final case class Message(level: Level, value: String, ex: Option[Throwable])
