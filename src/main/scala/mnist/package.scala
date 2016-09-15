import java.util.zip.GZIPInputStream

import fs2._

package object mnist {

  import decoder.idx._

  private[datasets] val trainImgZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/train-images-idx3-ubyte.gz"))
  }

  private[datasets] val trainLabelsZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/train-labels-idx1-ubyte.gz"))
  }

  private[datasets] val testImgZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/t10k-images-idx3-ubyte.gz"))
  }

  private[datasets] val testLabelsZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/t10k-labels-idx1-ubyte.gz"))
  }

  private[datasets] lazy val imageTraining = idxDecoder(trainImgZip)
  private[datasets] lazy val labelTraining = idxDecoder(trainLabelsZip)

  private[datasets] lazy val imageTesting = idxDecoder(testImgZip)
  private[datasets] lazy val labelTesting = idxDecoder(testLabelsZip)

  sealed trait SetType
  object SetType {
    case object Training extends SetType
    case object Testing extends SetType
  }

  case class MnistData(images: Task[IdxFile], labels: Task[IdxFile], annotation: SetType)

  lazy val trainingSet = MnistData(imageTraining, labelTraining, SetType.Training)
  lazy val testingSet = MnistData(imageTesting, labelTesting, SetType.Testing)

}