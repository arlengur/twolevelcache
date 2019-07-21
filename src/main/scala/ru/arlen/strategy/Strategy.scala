package ru.arlen.strategy

import collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap

/** Contains cache strategies */
object Strategy {
  sealed abstract class CacheStrategy[K] {
    protected val store = new ConcurrentHashMap[K, Long]().asScala

    def put(key: K): Unit

    def remove(key: K): Unit =
      if (store.contains(key)) store.remove(key)

    def getReplacedKey(): K = {
      val (key, _) = store.minBy { case (k, v) => v }
      key
    }

    def clear(): Unit = store.clear()

    def contains(key: K): Boolean = store.contains(key)
  }

  /** Least Frequently Used - вытеснение редко используемых */
  case class LFU[K]() extends CacheStrategy[K] {
    def put(key: K): Unit =
      store.updateWith(key)({
        case Some(count) => Some(count + 1)
        case None        => Some(1)
      })
  }

  /** Least Recently Used - вытеснение давно неиспользуемых */
  case class LRU[K]() extends CacheStrategy[K] {
    def put(key: K): Unit =
      store.put(key, System.nanoTime())
  }

  /** Most Recently Used - вытеснение недавно используемых */
  case class MRU[K]() extends CacheStrategy[K] {
    def put(key: K): Unit =
      store.put(key, System.nanoTime())
    override def getReplacedKey(): K = {
      val (key, _) = store.maxBy { case (k, v) => v }
      key
    }
  }
}
