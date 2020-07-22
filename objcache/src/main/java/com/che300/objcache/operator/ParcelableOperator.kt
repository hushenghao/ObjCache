package com.che300.objcache.operator

import android.os.Parcel
import android.os.Parcelable
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.util.Files

/**
 * Android序列化支持，不支持反序列化
 */
@PublishedApi
@KeyFactor("Parcelable")
internal open class ParcelableOperator<T : Parcelable> : CacheOperator<T> {

    override fun get(key: CacheKey, default: T?): T? {
        throw IllegalArgumentException("无法解析Parcelable, Parcelable.CREATOR未指定")
    }

    override fun put(key: CacheKey, value: T): Boolean {
        val cacheFile = key.cacheFile()
        val parcel = Parcel.obtain()
        value.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val byteArray = parcel.marshall()

        Files.writeBytes(cacheFile, byteArray)
        parcel.recycle()
        return true
    }

    /**
     * 补充了Parcelable反序列化操作
     */
    @PublishedApi
    internal open class Get<T : Parcelable>(private val creator: Parcelable.Creator<T>) :
        ParcelableOperator<T>() {

        override fun get(key: CacheKey, default: T?): T? {
            val cacheFile = key.cacheFile()
            val byteArray = Files.readBytes(cacheFile)
            var result: T? = default
            if (byteArray != null) {
                val parcel = Parcel.obtain()
                parcel.unmarshall(byteArray, 0, byteArray.size)
                parcel.setDataPosition(0)
                try {
                    result = creator.createFromParcel(parcel)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    parcel.recycle()
                }
            }
            return result ?: default
        }
    }
}