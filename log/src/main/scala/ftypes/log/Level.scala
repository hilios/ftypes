package ftypes.log

import cats.{Order, Show}

sealed trait Level { self =>

  val name: String = self.getClass.getSimpleName.toLowerCase.stripSuffix("$")

  def apply(message: String, exception: Option[Throwable]): Message =
    Message(level = self, message, exception)

  def unapply(message: Message): Option[(String, Option[Throwable])] =
    if (message.level == self) Some((message.value, message.throwable))
    else None
}

object Level {
  final case object Off extends Level
  final case object Trace extends Level
  final case object Debug extends Level
  final case object Info extends Level
  final case object Warn extends Level
  final case object Error extends Level
  final case object All extends Level

  implicit val showLevel: Show[Level] = {
    case level @ Level.Trace => s"${Console.BOLD}[${level.name}]"
    case level @ Level.Debug => s"${Console.CYAN}[${level.name}]"
    case level @ Level.Info  => s"${Console.WHITE}[${level.name}]"
    case level @ Level.Warn  => s"${Console.YELLOW}[${level.name}]"
    case level @ Level.Error => s"${Console.RED}[${level.name}]"
    case level @ _           => s"${Console.UNDERLINED}[${level.name}]"
  }

  implicit val logLevelOrdering: Ordering[Level] = (x: Level, y: Level) =>
    (x, y) match {
      case (a, b) if a == b => 0
      case (Off, _)         => 1
      case (_, Off)         => -1
      case (Error, _)       => 1
      case (_, Error)       => -1
      case (Warn, _)        => 1
      case (_, Warn)        => -1
      case (Info, _)        => 1
      case (_, Info)        => -1
      case (Debug, _)       => 1
      case (_, Debug)       => -1
      case (Trace, _)       => 1
      case (_, Trace)       => -1
      case (All, _)         => 1
      case (_, All)         => -1
    }

  implicit val logLevelOrder: Order[Level] = Order.fromOrdering
}
