package ru.arlen.cache

import ru.arlen.strategy.Strategy
import ru.arlen.strategy.Strategy.CacheStrategy
import ru.arlen.strategy.Strategy.LFU
import ru.arlen.strategy.Strategy.MRU
import ru.arlen.strategy.Strategy.LRU

import org.log4s._
import pureconfig.generic.auto._

case class Config[K <: Serializable, V <: Serializable](cache: TwoLevelCache[K, V])

/** Two level cache implementation */
case class TwoLevelCache[K <: Serializable, V <: Serializable](
    memCapacity: Int,
    fileCapacity: Int,
    cacheStrategy: String
) extends Cache[K, V] {
  private val logger: Logger = getLogger
  private val memoryCache    = new InMemoryCache[K, V]()
  private val fileCache      = new FileCache[K, V]()
  private val strategy = cacheStrategy match {
    case "LFU" => LFU[K]()
    case "LRU" => LRU[K]()
    case "MRU" => MRU[K]()
    case _     => LFU[K]()
  }
  private val mCapacity = memCapacity
  private val fCapacity = fileCapacity

  def put(key: K, value: V): Unit = {
    if (memoryCache.contains(key) || memoryCache.size < mCapacity) {
      logger.debug(s"Put object with key=${key} to the 1st level")
      memoryCache.put(key, value)
      if (fileCache.contains(key))
        fileCache.remove(key)
    } else if (fileCache.contains(key) || fileCache.size < fCapacity) {
      logger.debug(s"Put object with key=${key} to the 2st level")
      fileCache.put(key, value)
    } else {
      val replacedKey = strategy.getReplacedKey();
      if (memoryCache.contains(replacedKey)) {
        logger.debug(s"Replace object with key=${replacedKey} from 1st level")
        memoryCache.remove(replacedKey);
        memoryCache.put(key, value);
      } else if (fileCache.contains(replacedKey)) {
        logger.debug(s"Replace object with key=${replacedKey} from 2nd level")
        fileCache.remove(replacedKey);
        fileCache.put(key, value);
      }
    }
    if (!strategy.contains(key)) {
      logger.debug(s"Put object with key=${key} to strategy")
      strategy.put(key)
    }
  }

  def get(key: K): Option[V] =
    if (memoryCache.contains(key)) {
      strategy.put(key)
      memoryCache.get(key)
    } else if (fileCache.contains(key)) {
      strategy.put(key)
      fileCache.get(key)
    } else None

  def remove(key: K): Unit = {
    if (memoryCache.contains(key)) {
      logger.debug(s"Remove object with key=${key} from 1st level")
      strategy.remove(key)
      memoryCache.remove(key)
    } else if (fileCache.contains(key)) {
      logger.debug(s"Remove object with key=${key} from 2st level")
      strategy.remove(key)
      fileCache.remove(key)
    }
  }
  def size(): Int = memoryCache.size + fileCache.size

  def clear(): Unit = {
    memoryCache.clear()
    fileCache.clear()
    strategy.clear()
  }

  def contains(key: K): Boolean = memoryCache.contains(key) || fileCache.contains(key)
}
