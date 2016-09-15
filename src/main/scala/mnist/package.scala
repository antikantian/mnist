import java.util.zip.GZIPInputStream

import fs2._

package object mnist {

  import decoder.idx._

  private[mnist] val trainImgZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/train-images-idx3-ubyte.gz"))
  }

  private[mnist] val trainLabelsZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/train-labels-idx1-ubyte.gz"))
  }

  private[mnist] val testImgZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/t10k-images-idx3-ubyte.gz"))
  }

  private[mnist] val testLabelsZip = Task.delay {
    new GZIPInputStream(getClass.getResourceAsStream("/mnist/t10k-labels-idx1-ubyte.gz"))
  }

  private[mnist] lazy val imageTraining = idxDecoder(trainImgZip)
  private[mnist] lazy val labelTraining = idxDecoder(trainLabelsZip)

  private[mnist] lazy val imageTesting = idxDecoder(testImgZip)
  private[mnist] lazy val labelTesting = idxDecoder(testLabelsZip)

  sealed trait SetType
  object SetType {
    case object Training extends SetType
    case object Testing extends SetType
  }

  case class MnistData(images: Task[IdxFile], labels: Task[IdxFile], annotation: SetType)

  lazy val trainingSet = MnistData(imageTraining, labelTraining, SetType.Training)
  lazy val testingSet = MnistData(imageTesting, labelTesting, SetType.Testing)

}