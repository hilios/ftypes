package ftypes

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

package object kafka {
  type ByteArray = Array[Byte]
  type SingleMessage[T] = (String, T)
  type DefaultConsumerRecord = ConsumerRecord[ByteArray, ByteArray]
  type DefaultProducerRecord = ProducerRecord[ByteArray, ByteArray]
}
