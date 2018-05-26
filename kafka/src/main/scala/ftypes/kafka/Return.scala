package ftypes.kafka

sealed trait Return {
  def record: DefaultConsumerRecord
}

object Return {
  final case class Ack(record: DefaultConsumerRecord)   extends Return
  final case class Error(record: DefaultConsumerRecord) extends Return

  def apply[F[_]](consumerRecord: DefaultConsumerRecord): Return = new Return {
    def record: DefaultConsumerRecord = consumerRecord
  }
}
