package com.che300.objcache

import android.content.Context
import android.os.Parcelable
import com.che300.objcache.cache.ObjCacheDispatcher
import com.che300.objcache.operator.*
import com.che300.objcache.request.EditRequest
import com.che300.objcache.request.GetRequest
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.Serializable
import java.lang.reflect.Type

class ObjCache internal constructor(
    internal val context: Context,
    internal val cacheDir: File,
    internal val maxMemoryCount: Int,
    cacheOperators: HashMap<Type, CacheOperator<*>>
) {

    class Builder(private val context: Context) {

        private var cacheDir: File = Utils.defaultCacheDir(context)
        private var maxMemoryCount = DEFAULT_MEMORY_COUNT
        private val cacheOperators = HashMap<Type, CacheOperator<*>>()

        fun cacheDir(cacheDir: File): Builder {
            this.cacheDir = cacheDir
            return this
        }

        fun maxMemoryCount(maxCount: Int): Builder {
            this.maxMemoryCount = maxCount
            return this
        }

        fun addCacheOperator(type: Type, cacheOperator: CacheOperator<*>): Builder {
            this.cacheOperators[type] = cacheOperator
            return this
        }

        fun debug(debug: Boolean): Builder {
            Utils.debug = debug
            return this
        }

        fun create(): ObjCache {
            return ObjCache(context, cacheDir, maxMemoryCount, cacheOperators)
        }
    }

    internal val staticCacheOperatorManager = CacheOperatorManager()
    internal val cacheDispatcher = ObjCacheDispatcher(this)

    init {
        default = this

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        } else if (!cacheDir.isDirectory) {
            throw IllegalArgumentException("cacheDir必须是文件夹: " + cacheDir.path)
        }

        staticCacheOperatorManager.register(Int::class.java, SpOperator.Int())
        staticCacheOperatorManager.register(Long::class.java, SpOperator.Long())
        staticCacheOperatorManager.register(Float::class.java, SpOperator.Float())
        staticCacheOperatorManager.register(Boolean::class.java, SpOperator.Boolean())
        staticCacheOperatorManager.register(String::class.java, SpOperator.String())
        staticCacheOperatorManager.register(
            Parcelable::class.java,
            ParcelableOperator<Parcelable>()
        )
        staticCacheOperatorManager.register(Serializable::class.java, SerializableOperator())
        staticCacheOperatorManager.register(JSONObject::class.java, JSONObjectOperator())
        staticCacheOperatorManager.register(JSONArray::class.java, JSONArrayOperator())
        for (entry in cacheOperators) {
            staticCacheOperatorManager.register(entry.key, entry.value)
        }
    }

    companion object {

        private const val DEFAULT_MEMORY_COUNT = 1000
        internal const val LOG_T = "ObjCache"

        private var default: ObjCache? = null

        internal fun default(): ObjCache {
            return checkNotNull(default) { "" }
        }

        fun get(): GetRequest {
            return GetRequest()
        }

        fun edit(): EditRequest {
            return EditRequest()
        }

        fun clear() {
            default().cacheDispatcher.clear()
        }
    }
}