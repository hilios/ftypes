package ftypes.log

import java.io.{PrintWriter, StringWriter}

import cats.Show

final case class Message(level: Level, value: String, throwable: Option[Throwable])

object Message {

  def unapply(msg: Message): Option[(Level, String, Option[Throwable])] = Some((msg.level, msg.value, msg.throwable))

  implicit val consoleLogMessage: Show[Message] = new Show[Message] {

    def getStackTrace(throwable: Option[Throwable]): String =
      throwable
        .map { throwable =>
          val sw = new StringWriter()
          val pw = new PrintWriter(sw, true)
          pw.write("\n")
          throwable.printStackTrace(pw)
          sw.getBuffer.toString
        }
        .getOrElse("")

    def show(log: Message): String =
      s"${Show[Level].show(log.level)} ${log.value}${getStackTrace(log.throwable)}${Console.RESET}"
  }
}
