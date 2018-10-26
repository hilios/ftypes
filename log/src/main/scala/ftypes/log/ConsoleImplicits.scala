package ftypes.log

import java.io.{PrintWriter, StringWriter}

import cats.Show

trait ConsoleImplicits {
  
  implicit val consoleColors: Colors = {
    case Message(Level.Trace, _, _) => Console.BOLD
    case Message(Level.Debug, _, _) => Console.CYAN
    case Message(Level.Info,  _, _) => Console.WHITE
    case Message(Level.Warn,  _, _) => Console.YELLOW
    case Message(Level.Error, _, _) => Console.RED
    case _                          => Console.UNDERLINED
  }

  implicit def consoleLogMessage(implicit colors: Colors): Show[Message] = new Show[Message] {

    def level(logMessage: Message): String = s"[${logMessage.level.name}]"

    def getStackTrace(ex: Option[Throwable]): String = ex.map { throwable =>
      val sw = new StringWriter()
      val pw = new PrintWriter(sw, true)
      pw.write("\n")
      throwable.printStackTrace(pw)
      sw.getBuffer.toString
    }.getOrElse("")

    def show(log: Message): String =
      s"${colors.get(log)}${level(log)} ${log.value}${getStackTrace(log.ex)}${Console.RESET}"
  }
}
