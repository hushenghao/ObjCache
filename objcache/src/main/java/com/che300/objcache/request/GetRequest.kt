package com.che300.objcache.request

import android.os.Parcelable
import com.che300.objcache.ObjCache
import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.cache.ObjCacheDispatcher
import com.che300.objcache.operator.ParcelableOperator
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.lang.reflect.Type

class GetRequest : CacheStrategy<GetRequest> {

    internal var strategy: Int = CacheStrategy.ALL
    internal lateinit var key: String

    override fun cacheStrategy(strategy: Int): GetRequest {
        this.strategy = strategy
        return this
    }

    private fun cacheDispatcher(): ObjCacheDispatcher {
        return ObjCache.default().cacheDispatcher
    }

    fun getInt(key: String, default: Int = 0): Int {
        return get(key, Int::class.java, default) ?: 0
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return get(key, Long::class.java, default) ?: 0L
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return get(key, Float::class.java, default) ?: 0f
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return get(key, Boolean::class.java, default) ?: false
    }

    fun getString(key: String, default: String? = null): String? {
        return get(key, String::class.java, default)
    }

    fun <T : Parcelable> getParcelable(
        key: String,
        creator: Parcelable.Creator<T>,
        default: T? = null
    ): T? {
        this.key = key
        return cacheDispatcher().get(
            this,
            default,
            ParcelableOperator.Get(creator)
        )
    }

    fun <T : Serializable> getSerializable(key: String, default: T? = null): T? {
        return get(key, Serializable::class.java, default) as? T
    }

    fun getJSONObject(key: String, default: JSONObject? = null): JSONObject? {
        return get(key, JSONObject::class.java, default)
    }

    fun getJSONArray(key: String, default: JSONArray? = null): JSONArray? {
        return get(key, JSONArray::class.java, default)
    }

    fun <T> get(key: String, clazz: Class<T>, default: T?): T? {
        return get(key, clazz as Type, default)
    }

    fun <T> get(key: String, type: Type, default: T?): T? {
        this.key = key
        return cacheDispatcher().get(this, type, default)
    }
}