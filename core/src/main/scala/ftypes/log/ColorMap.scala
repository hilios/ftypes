package ftypes.log

trait ColorMap {
  def color(log: LogMessage): String
}
