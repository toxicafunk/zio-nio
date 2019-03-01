package scalaz.nio.channels

import java.io.IOException
import java.nio.channels.{ Selector => JSelector }

import scalaz.nio.channels.spi.SelectorProvider
import scalaz.nio.io._
import scalaz.zio.IO
import scalaz.zio.duration.Duration

import scala.collection.JavaConverters

class Selector(private[nio] val selector: JSelector) {

  final val isOpen: IO[Nothing, Boolean] = IO.sync(selector.isOpen)

  final val provider: IO[Nothing, SelectorProvider] =
    IO.sync(selector.provider()).map(new SelectorProvider(_))

  final val keys: IO[Nothing, Set[SelectionKey]] =
    IO.sync(selector.keys()).map { keys =>
      JavaConverters.asScalaSet(keys).toSet.map(new SelectionKey(_))
    }

  final val selectedKeys: IO[Nothing, Set[SelectionKey]] =
    IO.sync(selector.selectedKeys()).map { keys =>
      JavaConverters.asScalaSet(keys).toSet.map(new SelectionKey(_))
    }

  final def removeKey(key: SelectionKey): IO[Nothing, Unit] =
    IO.sync(selector.selectedKeys().remove(key.selectionKey)).void

  final val selectNow: IO[IOException, Int] =
    IO.syncCatch(selector.selectNow())(JustIOException)

  final def select(timeout: Duration): IO[IOException, Int] =
    IO.syncCatch(selector.select(timeout.toMillis))(JustIOException)

  final val select: IO[IOException, Int] =
    IO.syncCatch(selector.select())(JustIOException)

  final val wakeup: IO[Nothing, Selector] =
    IO.sync(selector.wakeup()).map(new Selector(_))

  final val close: IO[IOException, Unit] =
    IO.syncCatch(selector.close())(JustIOException).void
}

object Selector {

  final val make: IO[IOException, Selector] =
    IO.syncCatch(new Selector(JSelector.open()))(JustIOException)

}