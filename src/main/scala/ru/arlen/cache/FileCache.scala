package ru.arlen.cache

import org.log4s._
import java.nio.file.Path
import java.nio.file.Files
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.File
import java.io.ObjectInputStream
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConverters._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.util.Using

/** File cache implementation */
class FileCache[K <: Serializable, V <: Serializable] extends Cache[K, V] {
  private val logger: Logger = getLogger
  private val cache          = new ConcurrentHashMap[K, String]().asScala
  private val tempDir: Path  = Files.createTempDirectory("cache")

  def contains(key: K): Boolean = cache.contains(key)

  def get(key: K): Option[V] =
    if (cache.contains(key)) {
      val fileName = cache.get(key).get
      Using(new ObjectInputStream(new FileInputStream(new File(tempDir + File.separator + fileName)))) { reader =>
        reader.readObject().asInstanceOf[V]
      } match {
        case Success(value) => Option(value)
        case Failure(ex) =>
          logger.error(s"Can't read a file. ${fileName}: ${ex.getMessage}")
          None
      }
    } else {
      logger.debug(s"Object with key=${key} does not exist");
      None
    }

  def put(key: K, value: V): Unit = {
    val tmpFile = Files.createTempFile(tempDir, "", "").toFile()
    Using(new ObjectOutputStream(new FileOutputStream(tmpFile))) { writer =>
      writer.writeObject(value)
      writer.flush()
    } match {
      case Success(value) =>
        logger.debug(s"put ${key} -> ${tmpFile.getAbsolutePath}")
        cache.put(key, tmpFile.getName)
      case Failure(ex) =>
        logger.error("Can't write an object to a file " + tmpFile.getName() + ": " + ex.getMessage());
    }
  }

  def remove(key: K): Unit = {
    logger.debug(s"remove ${key}")
    val fileName    = cache.get(key).get
    val deletedFile = new File(tempDir + File.separator + fileName)
    deleteFile(deletedFile)
    cache.remove(key)
  }

  def size(): Int = cache.size

  def clear(): Unit = {
    logger.debug("clear file cache")
    val file = tempDir.toFile
    if (file.exists && file.isDirectory) {
      file.listFiles.filter(_.isFile()).foreach(deleteFile(_))
      deleteFile(file)
    }
    cache.clear
  }

  private def deleteFile(file: File): Unit =
    if (file.delete)
      logger.debug(s"Cache: ${file.getName} has been deleted")
    else
      logger.error(s"Can't delete: ${file.getName}")
}
