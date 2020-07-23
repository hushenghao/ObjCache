package com.che300.objcache.operator

import android.os.Parcel
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.util.Files
import java.io.*

/**
 * Java序列化缓存操作者
 */
@KeyFactor("Serializable")
internal class SerializableOperator : CacheOperator<Serializable> {

    override fun get(key: CacheKey, default: Serializable?): Serializable? {
        val cacheFile = key.cacheFile()
        try {
            val obj = ObjectInputStream(FileInputStream(cacheFile)).use {
                it.readObject() as? Serializable
            }
            return obj ?: default
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return default
    }

    override fun put(key: CacheKey, value: Serializable): Boolean {
        val cacheFile = key.cacheFile()
        try {
            ObjectOutputStream(FileOutputStream(cacheFile)).use {
                it.writeObject(value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * 使用Parcel实现的Serializable序列化操作
     */
    @KeyFactor("FastSerializable")
    internal class Fast : CacheOperator<Serializable> {

        override fun get(key: CacheKey, default: Serializable?): Serializable? {
            val cacheFile = key.cacheFile()
            var result = default
            val readBytes = Files.readBytes(cacheFile)
            if (readBytes != null) {
                val parcel = Parcel.obtain()
                parcel.unmarshall(readBytes, 0, readBytes.size)
                parcel.setDataPosition(0)
                try {
                    result = parcel.readSerializable()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    parcel.recycle()
                }
            }
            return result
        }

        override fun put(key: CacheKey, value: Serializable): Boolean {
            val cacheFile = key.cacheFile()
            val parcel = Parcel.obtain()
            parcel.writeSerializable(value)
            parcel.setDataPosition(0)
            val bytes = parcel.marshall()
            Files.writeBytes(cacheFile, bytes)
            parcel.recycle()
            return true
        }
    }

}
