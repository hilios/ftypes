package ftypes.log

import java.io.{PrintWriter, StringWriter}

import cats.Show

trait ConsoleImplicits {
  
  implicit val consoleColors: Colors = new Colors {
    def get(log: LogMessage): String = log match {
      case _: LogMessage.TraceLog => Console.BOLD
      case _: LogMessage.DebugLog => Console.CYAN
      case _: LogMessage.InfoLog  => Console.WHITE
      case _: LogMessage.WarnLog  => Console.YELLOW
      case _: LogMessage.ErrorLog => Console.RED
    }
  }

  implicit def consoleLogMessage(implicit colors: Colors): Show[LogMessage] = new Show[LogMessage] {

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
      s"${colors.get(log)}${level(log)} ${log.message}${getStackTrace(log.ex)}${Console.RESET}"
  }
}
