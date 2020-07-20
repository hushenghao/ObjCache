package com.che300.objcache.operator

import android.os.Parcel
import android.os.Parcelable
import com.che300.objcache.util.Files
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey

/**
 * Android序列化支持
 */
@PublishedApi
@KeyFactor("Parcelable")
internal open class ParcelableOperator<T : Parcelable> : CacheOperator<T> {

    override fun get(key: CacheKey, default: T?): T? {
        throw IllegalArgumentException("无法解析Parcelable, CREATOR未设置")
    }

    override fun put(key: CacheKey, value: T?): Boolean {
        val cacheFile = key.cacheFile()
        if (value == null) {
            return cacheFile.delete()
        }
        val parcel = Parcel.obtain()
        value.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val byteArray = parcel.marshall()

        Files.writeBytes(cacheFile, byteArray)
        parcel.recycle()
        return true
    }

    @PublishedApi
    internal class Get<T : Parcelable>(private val creator: Parcelable.Creator<T>) :
        ParcelableOperator<T>() {

        override fun get(key: CacheKey, default: T?): T? {
            val cacheFile = key.cacheFile()
            val parcel = Parcel.obtain()
            var result: T? = null
            try {
                val byteArray = Files.readBytes(cacheFile)
                if (byteArray != null) {
                    parcel.unmarshall(byteArray, 0, byteArray.size)
                    parcel.setDataPosition(0)

                    result = creator.createFromParcel(parcel)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                parcel.recycle()
            }
            return result ?: default
        }
    }
}