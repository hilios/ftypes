package ftypes.log

import java.io.{PrintWriter, StringWriter}

import cats.{Order, Show}

trait Implicits {
  
  implicit val colorMap = new ColorMap {
    override def color(log: LogMessage): String = log match {
      case _: Trace => Console.BOLD
      case _: Debug => Console.CYAN
      case _: Info  => Console.WHITE
      case _: Warn  => Console.YELLOW
      case _: Error => Console.RED
    }
  }

  implicit def showLogMessage(implicit colors: ColorMap): Show[LogMessage] = new Show[LogMessage] {

    def level(logMessage: LogMessage): String =
      s"[${logMessage.getClass.getSimpleName.toLowerCase}]"

    def getStackTrace(ex: Option[Throwable]): String = ex.map { throwable =>
      val sw = new StringWriter()
      val pw = new PrintWriter(sw, true)
      pw.write("\n")
      throwable.printStackTrace(pw)
      sw.getBuffer.toString
    }.getOrElse("")

    def show(log: LogMessage): String =
      s"${colors.color(log)}${level(log)} ${log.message}${getStackTrace(log.ex)}${Console.RESET}"
  }

  implicit val logLevelOrdering: Ordering[LogLevel] =
    (x: LogLevel, y: LogLevel) =>
      (x, y) match {
        case (a, b) if a == b => 0
        case (Off, _)         => 1
        case (_, Off)         => -1
        case (ErrorLevel, _)  => 1
        case (_, ErrorLevel)  => -1
        case (WarnLevel, _)   => 1
        case (_, WarnLevel)   => -1
        case (InfoLevel, _)   => 1
        case (_, InfoLevel)   => -1
        case (DebugLevel, _)  => 1
        case (_, DebugLevel)  => -1
        case (TraceLevel, _)  => 1
        case (_, TraceLevel)  => -1
        case (All, _)         => 1
        case (_, All)         => -1
      }

  implicit val logLevelOrder: Order[LogLevel] = Order.fromOrdering
}
