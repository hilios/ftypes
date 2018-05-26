package ftypes

import org.apache.kafka.clients.consumer.ConsumerRecord

package object kafka {
  type ByteArray = Array[Byte]
  type SingleMessage[T] = (String, T)
  type DefaultConsumerRecord = ConsumerRecord[ByteArray, ByteArray]
}
