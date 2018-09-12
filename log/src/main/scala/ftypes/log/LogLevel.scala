package ftypes.log

import cats.Order

sealed trait LogLevel

object LogLevel {
  final case object Off   extends LogLevel
  final case object Trace extends LogLevel
  final case object Debug extends LogLevel
  final case object Info  extends LogLevel
  final case object Warn  extends LogLevel
  final case object Error extends LogLevel
  final case object All   extends LogLevel

  implicit val logLevelOrdering: Ordering[LogLevel] = (x: LogLevel, y: LogLevel) => (x, y) match {
    case (a, b) if a == b => 0
    case (Off, _)   => 1
    case (_, Off)   => -1
    case (Error, _) => 1
    case (_, Error) => -1
    case (Warn, _)  => 1
    case (_, Warn)  => -1
    case (Info, _)  => 1
    case (_, Info)  => -1
    case (Debug, _) => 1
    case (_, Debug) => -1
    case (Trace, _) => 1
    case (_, Trace) => -1
    case (All, _)   => 1
    case (_, All)   => -1
  }

  implicit val logLevelOrder: Order[LogLevel] = Order.fromOrdering
}
