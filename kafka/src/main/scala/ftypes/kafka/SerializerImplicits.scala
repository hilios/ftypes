package ftypes.kafka

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import ftypes.kafka.serializers.{KafkaDecoder, KafkaEncoder}

trait SerializerImplicits {
  implicit val floatKafkaEncoder: KafkaEncoder[Float] = (value: Float) => ByteBuffer.allocate(4).putFloat(value).array()
  implicit val floatKafkaDecoder: KafkaDecoder[Float] = (value: Array[Byte]) => ByteBuffer.wrap(value).getFloat

  implicit val doubleKafkaEncoder: KafkaEncoder[Double] = (value: Double) => ByteBuffer.allocate(8).putDouble(value).array()
  implicit val doubleKafkaDecoder: KafkaDecoder[Double] = (value: Array[Byte]) => ByteBuffer.wrap(value).getDouble

  implicit val intKafkaEncoder: KafkaEncoder[Int] = (value: Int) => ByteBuffer.allocate(4).putInt(value).array()
  implicit val intKafkaDecoder: KafkaDecoder[Int] = (value: Array[Byte]) => ByteBuffer.wrap(value).getInt

  implicit val longKafkaEncoder: KafkaEncoder[Long] = (value: Long) => ByteBuffer.allocate(8).putLong(value).array()
  implicit val longKafkaDecoder: KafkaDecoder[Long] = (value: Array[Byte]) => ByteBuffer.wrap(value).getLong

  implicit val shortKafkaEncoder: KafkaEncoder[Short] = (value: Short) => ByteBuffer.allocate(2).putShort(value).array()
  implicit val shortKafkaDecoder: KafkaDecoder[Short] = (value: Array[Byte]) => ByteBuffer.wrap(value).getShort

  implicit val stringKafkaEncoder: KafkaEncoder[String] = (value: String) => value.getBytes(StandardCharsets.UTF_8)
  implicit val stringKafkaDecoder: KafkaDecoder[String] = (value: Array[Byte]) => new String(value, StandardCharsets.UTF_8)
}
