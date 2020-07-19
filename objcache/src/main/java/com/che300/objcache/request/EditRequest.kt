package com.che300.objcache.request

import android.os.Parcelable
import com.che300.objcache.ObjCache
import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.cache.ObjCacheDispatcher
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.lang.reflect.Type

class EditRequest : CacheStrategy<EditRequest> {

    internal var strategy: Int = CacheStrategy.ALL
    internal lateinit var key: String

    override fun cacheStrategy(strategy: Int): EditRequest {
        this.strategy = strategy
        return this
    }

    private fun cacheDispatcher(): ObjCacheDispatcher {
        return ObjCache.default().cacheDispatcher
    }

    fun putInt(key: String, value: Int): Boolean {
        return put(key, Int::class.java, value)
    }

    fun putLong(key: String, value: Long): Boolean {
        return put(key, Long::class.java, value)
    }

    fun putFloat(key: String, value: Float): Boolean {
        return put(key, Float::class.java, value)
    }

    fun putBoolean(key: String, value: Boolean): Boolean {
        return put(key, Boolean::class.java, value)
    }

    fun putString(key: String, value: String?): Boolean {
        return put(key, String::class.java, value)
    }

    fun <T : Parcelable> putParcelable(key: String, value: T?): Boolean {
        return put(key, Parcelable::class.java, value)
    }

    fun <T : Serializable> putSerializable(key: String, value: T?): Boolean {
        return put(key, Serializable::class.java, value)
    }

    fun putJSONObject(key: String, value: JSONObject?): Boolean {
        return put(key, JSONObject::class.java, value)
    }

    fun putJSONArray(key: String, value: JSONArray?): Boolean {
        return put(key, JSONArray::class.java, value)
    }

    fun <T> put(key: String, type: Class<T>, value: T?): Boolean {
        return put(key, type as Type, value)
    }

    fun <T> put(key: String, type: Type, value: T?): Boolean {
        this.key = key
        return cacheDispatcher().put(this, type, value)
    }
}