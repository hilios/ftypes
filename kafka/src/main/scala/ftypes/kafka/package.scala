package ftypes

import java.nio.charset.StandardCharsets

package object kafka {
  type ByteArray = Array[Byte]

  implicit val stringKafkaEncoder: KafkaEncoder[String] = (value: String) => value.getBytes(StandardCharsets.UTF_8)
  implicit val stringKafkaDecoder: KafkaDecoder[String] = (value: Array[Byte]) => value.map(_.toChar).mkString
}
