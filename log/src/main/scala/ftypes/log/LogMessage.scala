package ftypes.log

sealed abstract class LogMessage(val level: LogLevel) {
  def message: String
  def ex: Option[Throwable]
}

object LogMessage {
  final case class TraceLog(message: String, ex: Option[Throwable]) extends LogMessage(LogLevel.Trace)
  final case class DebugLog(message: String, ex: Option[Throwable]) extends LogMessage(LogLevel.Debug)
  final case class InfoLog (message: String, ex: Option[Throwable]) extends LogMessage(LogLevel.Info)
  final case class WarnLog (message: String, ex: Option[Throwable]) extends LogMessage(LogLevel.Warn)
  final case class ErrorLog(message: String, ex: Option[Throwable]) extends LogMessage(LogLevel.Error)
}
