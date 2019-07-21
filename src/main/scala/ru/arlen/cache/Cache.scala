package ru.arlen.cache

trait Cache[K, V] {
    def put(key: K, value: V): Unit
    def get(key: K): Option[V]
    def remove(key: K): Unit
    def size(): Int
    def clear(): Unit
    def contains(key: K): Boolean
}