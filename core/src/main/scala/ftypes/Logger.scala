package ftypes

import org.slf4j
import org.slf4j.LoggerFactory

trait Logger {
  def get: slf4j.Logger
}

object Logger {
  implicit def apply: Logger = macro LoggingMacros.materializeLogger
  
  def get: slf4j.Logger = macro LoggingMacros.materializeLoggerFactory
  
  def forClass[T]: slf4j.Logger = macro LoggingMacros.materializeLoggerFactoryT[T]
  def withName(name: String): slf4j.Logger = LoggerFactory.getLogger(name)
}
