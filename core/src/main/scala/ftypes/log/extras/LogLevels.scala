package ftypes.log.extras

trait LogLevels { self =>
  sealed abstract class LogLevel(val weight: Int, val color: String) {
    lazy val name: String = s"[${this.getClass.getSimpleName.stripSuffix("$").toLowerCase()}]"
  }
  final case object Trace extends LogLevel(0, Console.BLUE)
  final case object Debug extends LogLevel(1, Console.MAGENTA)
  final case object Info  extends LogLevel(2, Console.CYAN)
  final case object Warn  extends LogLevel(3, Console.YELLOW)
  final case object Error extends LogLevel(4, Console.RED)
}
