package ftypes

import org.apache.kafka.clients.consumer.{Consumer, ConsumerRecord}
import org.apache.kafka.clients.producer.{Producer, ProducerRecord}

package object kafka {
  type ByteArray = Array[Byte]
  type SingleMessage[T] = (String, T)
  type DefaultConsumer = Consumer[ByteArray, ByteArray]
  type DefaultProducer = Producer[ByteArray, ByteArray]
  type DefaultConsumerRecord = ConsumerRecord[ByteArray, ByteArray]
  type DefaultProducerRecord = ProducerRecord[ByteArray, ByteArray]
}
