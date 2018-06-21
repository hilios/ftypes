package ftypes.log

trait DataTypes { self =>

  sealed trait LogLevel
  final case object Off        extends LogLevel
  final case object TraceLevel extends LogLevel
  final case object DebugLevel extends LogLevel
  final case object InfoLevel  extends LogLevel
  final case object WarnLevel  extends LogLevel
  final case object ErrorLevel extends LogLevel
  final case object All        extends LogLevel

  sealed abstract class LogMessage(val level: LogLevel) {
    def message: String
    def ex: Option[Throwable]
  }
  final case class Trace(message: String, ex: Option[Throwable]) extends LogMessage(TraceLevel)
  final case class Debug(message: String, ex: Option[Throwable]) extends LogMessage(DebugLevel)
  final case class Info (message: String, ex: Option[Throwable]) extends LogMessage(InfoLevel)
  final case class Warn (message: String, ex: Option[Throwable]) extends LogMessage(WarnLevel)
  final case class Error(message: String, ex: Option[Throwable]) extends LogMessage(ErrorLevel)
}
