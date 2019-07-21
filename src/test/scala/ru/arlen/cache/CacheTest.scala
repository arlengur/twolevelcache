package ru.arlen.cache

import org.scalatest._
import ru.arlen.strategy.Strategy.LFU
import ru.arlen.strategy.Strategy.LRU
import ru.arlen.strategy.Strategy.MRU
import pureconfig.loadConfig
import pureconfig.generic.auto._
import org.log4s._

class CacheTest extends WordSpec with Matchers {
  val logger: Logger = getLogger

  "In memory cache" should {
    "correctly work" in {
      val cache = new InMemoryCache[String, String]()
      cache.put("1", "one")
      cache.put("2", "two")
      cache.put("3", "three")

      cache.size shouldBe 3
      cache.get("1") shouldBe Some("one")
      cache.get("2") shouldBe Some("two")
      cache.get("3") shouldBe Some("three")

      cache.remove("2")
      cache.get("2") shouldBe None

      cache.contains("2") shouldBe false
      cache.contains("3") shouldBe true

      cache.clear()
      cache.size shouldBe 0
    }
  }

  "File cache" should {
    "correctly work" in {
      val cache = new FileCache[String, String]()
      cache.put("1", "one")
      cache.put("2", "two")
      cache.put("3", "three")

      cache.size shouldBe 3
      cache.get("1") shouldBe Some("one")
      cache.get("2") shouldBe Some("two")
      cache.get("3") shouldBe Some("three")

      cache.remove("2")
      cache.get("2") shouldBe None

      cache.contains("2") shouldBe false
      cache.contains("3") shouldBe true

      cache.clear()
      cache.size shouldBe 0
    }
  }

  "Two level cache with LFU strategy" should {
    "correctly work" in {
      case class Config[K <: Serializable, V <: Serializable](cacheLFU: TwoLevelCache[K, V])
      def config = loadConfig[Config[String, String]]

      config match {
        case Left(value) =>
          logger.error(s"Encountered the following errors reading the configuration: ${value.toList.mkString("\n")}")
        case Right(value) =>
          val cache = value.cacheLFU
          cache.put("1", "one")
          cache.put("2", "two")
          cache.put("3", "three")
          cache.put("4", "four")
          cache.put("5", "five")

          cache.size shouldBe 4
          cache.get("1") shouldBe None
          cache.get("2") shouldBe Some("two")
          cache.get("3") shouldBe Some("three")
          cache.get("4") shouldBe Some("four")
          cache.get("5") shouldBe Some("five")

          cache.remove("2")
          cache.get("2") shouldBe None
          cache.remove("3")
          cache.get("3") shouldBe None

          cache.contains("3") shouldBe false
          cache.contains("4") shouldBe true

          cache.clear()
          cache.size shouldBe 0
      }
    }
  }

  "Two level cache with LRU strategy" should {
    "correctly work" in {
      case class Config[K <: Serializable, V <: Serializable](cacheLRU: TwoLevelCache[K, V])
      def config = loadConfig[Config[String, String]]

      config match {
        case Left(value) =>
          logger.error(s"Encountered the following errors reading the configuration: ${value.toList.mkString("\n")}")
        case Right(value) =>
          val cache = value.cacheLRU
          cache.put("1", "one")
          cache.put("2", "two")
          cache.put("3", "three")
          cache.put("4", "four")
          cache.get("1")
          cache.put("5", "five")

          cache.size shouldBe 4
          cache.get("1") shouldBe Some("one")
          cache.get("2") shouldBe None
          cache.get("3") shouldBe Some("three")
          cache.get("4") shouldBe Some("four")
          cache.get("5") shouldBe Some("five")

          cache.clear()
          cache.size shouldBe 0
      }
    }
  }

  "Two level cache with MRU strategy" should {
    "correctly work" in {
      case class Config[K <: Serializable, V <: Serializable](cacheMRU: TwoLevelCache[K, V])
      def config = loadConfig[Config[String, String]]

      config match {
        case Left(value) =>
          logger.error(s"Encountered the following errors reading the configuration: ${value.toList.mkString("\n")}")
        case Right(value) =>
          val cache = value.cacheMRU
          cache.put("1", "one")
          cache.put("2", "two")
          cache.put("3", "three")
          cache.put("4", "four")
          cache.put("5", "five")

          cache.size shouldBe 4
          cache.get("1") shouldBe Some("one")
          cache.get("2") shouldBe Some("two")
          cache.get("3") shouldBe Some("three")
          cache.get("4") shouldBe None
          cache.get("5") shouldBe Some("five")

          cache.clear()
          cache.size shouldBe 0
      }
    }
  }

  "Two level cache with MRU strategy" should {
    "correctly move object from 2 to 1 level" in {
      case class Config[K <: Serializable, V <: Serializable](cacheMRU: TwoLevelCache[K, V])
      def config = loadConfig[Config[String, String]]

      config match {
        case Left(value) =>
          logger.error(s"Encountered the following errors reading the configuration: ${value.toList.mkString("\n")}")
        case Right(value) =>
          val cache = value.cacheMRU
          cache.put("1", "one")
          cache.put("2", "two")
          cache.put("3", "three")
          cache.put("4", "four")
          cache.remove("1")
          cache.put("4", "four")

          cache.size shouldBe 3
          cache.get("1") shouldBe None
          cache.get("2") shouldBe Some("two")
          cache.get("3") shouldBe Some("three")
          cache.get("4") shouldBe Some("four")

          cache.clear()
          cache.size shouldBe 0
      }
    }
  }
}
