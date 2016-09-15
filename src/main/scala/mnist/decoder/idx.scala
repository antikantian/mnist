package mnist.decoder

import java.io.InputStream
import java.nio.ByteBuffer

import fs2._
import fs2.io._

object idx {

  def idxDecoder(fis: Task[InputStream]): Task[IdxFile] = {

    def makeBuffer(chunkSize: Int): Array[Byte] =
      new Array[Byte](chunkSize)

    def segment(is: InputStream, buf: Array[Byte]): Task[Chunk[Byte]] =
      Task.delay(is.read(buf)).map(bytes => Chunk.bytes(buf, 0, bytes))

    for {
      is <- fis
      arr <- segment(is, makeBuffer(4))
      magic = MagicNumber(arr.toArray)
      dims <- segment(is, makeBuffer(magic.dims * 4))
      dimsizes = dims.toArray.grouped(4).map(ba => ByteBuffer.wrap(ba).getInt).toVector
      head = Header(magic.content, magic.dataType, magic.dims, dimsizes)
      samples = dimsizes.head
      datalen = dimsizes.tail.product * magic.dataType.byteLength
      ds = readInputStream(Task.now(is), datalen * 10).sliding(datalen).zipWithIndex.map { vecidx =>
        val (bvec, idx) = vecidx
        magic.content match {
          case IdxType.Images => IdxData.Image(idx, head.dimSizes(1), head.dimSizes(2), bvec.toArray)
          case IdxType.Labels => IdxData.Label(idx, bvec.toArray)
        }
      }
    } yield IdxFile(head, ds)
  }

  case class MagicNumber(bytes: Array[Byte]) {
    import DataType._

    val dataType: DataType = bytes.apply(2) match {
      case 0x08 => UByte
      case 0x09 => SByte
      case 0x0B => Short
      case 0x0C => Int
      case 0x0D => Float
      case 0x0E => Double
      case _    => Unknown
    }

    val dims: Int = bytes.apply(3).toInt

    val content: IdxType = ByteBuffer.wrap(bytes).getInt match {
      case 2049 => IdxType.Labels
      case 2051 => IdxType.Images
      case _    => IdxType.Unknown
    }
  }

  sealed trait DataType { def byteLength: Int }
  object DataType {
    case object UByte   extends DataType { val byteLength = 1 }
    case object SByte   extends DataType { val byteLength = 1 }
    case object Short   extends DataType { val byteLength = 2 }
    case object Int     extends DataType { val byteLength = 4 }
    case object Float   extends DataType { val byteLength = 4 }
    case object Double  extends DataType { val byteLength = 8 }
    case object Unknown extends DataType { val byteLength = 0 }
  }

  sealed trait IdxType extends Product with Serializable
  object IdxType {
    case object Images extends IdxType
    case object Labels extends IdxType
    case object Unknown extends IdxType
    case object Empty extends IdxType
  }

  sealed trait IdxDecodeError extends Exception
  object IdxDecodeError {
    case object InvalidMagicNumber extends IdxDecodeError
    case object InvalidDimArraySize extends IdxDecodeError
  }

  case class Header(content: IdxType, dataType: DataType, dims: Int, dimSizes: Vector[Int])

  case class IdxFile(header: Header, private[datasets] val data: Stream[Task, IdxData]) {
    def numSamples: Int = header.dimSizes.head
    def datastream = data.take(numSamples).flatMap(Stream.emit)
  }

  sealed trait IdxData {
    def sampleNum: Int
    def underlying: Array[Byte]
  }

  object IdxData {

    case class Label(sampleNum: Int, underlying: Array[Byte]) extends IdxData

    case class Image(sampleNum: Int, rows: Int, cols: Int, underlying: Array[Byte]) extends IdxData
  }
}