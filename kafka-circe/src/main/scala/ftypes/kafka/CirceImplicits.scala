package ftypes.kafka

import _root_.io.circe.parser.{decode => jsonDecoder}
import _root_.io.circe.{Decoder, Encoder, Printer}
import ftypes.kafka.serializers.{KafkaDecoder, KafkaEncoder}

trait CirceImplicits  extends SerializerImplicits {
  private[this] val defaultPrinter = Printer.noSpaces.copy(dropNullValues = true)

  implicit def circeKafkaEncoder[T](implicit encoder: Encoder[T], printer: Printer = defaultPrinter): KafkaEncoder[T] =
    (value: T) => stringKafkaEncoder.encode(printer.pretty(encoder.apply(value)))

  implicit def circeKafkaDecoder[T](implicit decoder: Decoder[T]): KafkaDecoder[T] =
    (value: Array[Byte]) => jsonDecoder[T](stringKafkaDecoder.decode(value)).fold(throw _, identity)
}
