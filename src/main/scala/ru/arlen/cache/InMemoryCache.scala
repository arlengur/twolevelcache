package ru.arlen.cache

import org.log4s._
import collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap

/** In memory cache implementation */
class InMemoryCache[K <: Serializable, V <: Serializable] extends Cache[K, V] {
  private val logger: Logger = getLogger
  private val cache          = new ConcurrentHashMap[K, V]().asScala

  def clear(): Unit = {
    logger.debug("clear in memory cache")
    cache.clear
  }
  def contains(key: K): Boolean = cache.contains(key)
  def get(key: K): Option[V] = {
    logger.debug(s"get key=${key}")
    cache.get(key)
  }
  def put(key: K, value: V): Unit = {
    logger.debug(s"put ${key} -> ${value}")
    cache.put(key, value)
  }
  def remove(key: K): Unit = {
    logger.debug(s"remove ${key}")
    cache.remove(key)
  }
  def size(): Int = cache.size
}
