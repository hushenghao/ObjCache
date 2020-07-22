package com.che300.objcache.operator

import android.os.Parcel
import android.os.Parcelable
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.util.Files

/**
 * Android序列化集合支持
 */
@PublishedApi
@KeyFactor("ParcelableList")
internal open class ParcelableListOperator : CacheOperator<List<Parcelable>> {

    override fun get(key: CacheKey, default: List<Parcelable>?): List<Parcelable>? {
        val cacheFile = key.cacheFile()
        val byteArray = Files.readBytes(cacheFile)
        val result: List<Parcelable>? = default
        if (byteArray != null) {
            val parcel = Parcel.obtain()
            parcel.unmarshall(byteArray, 0, byteArray.size)
            parcel.setDataPosition(0)
            try {
                val list = ArrayList<Parcelable>()
                parcel.readParcelableList(list, null)
                return list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                parcel.recycle()
            }
        }
        return result ?: default
    }

    override fun put(key: CacheKey, value: List<Parcelable>): Boolean {
        val cacheFile = key.cacheFile()
        val parcel = Parcel.obtain()
        parcel.writeParcelableList(value, 0)
        parcel.setDataPosition(0)
        val byteArray = parcel.marshall()

        Files.writeBytes(cacheFile, byteArray)
        parcel.recycle()
        return true
    }

}