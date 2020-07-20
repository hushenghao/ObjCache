package com.che300.objcache

import android.content.Context
import android.os.Parcelable
import com.che300.objcache.cache.LruDiskTrim
import com.che300.objcache.cache.ObjCacheDispatcher
import com.che300.objcache.operator.*
import com.che300.objcache.request.RequestBuilder
import com.che300.objcache.util.Files
import com.che300.objcache.util.Utils
import com.che300.objcache.util.logw
import java.io.File
import java.io.Serializable
import java.lang.reflect.Type

class ObjCache internal constructor(
    internal val context: Context,
    internal val cacheDir: File,
    internal val maxMemoryCount: Int,
    diskTrimStrategy: LruDiskTrim.Strategy,
    cacheOperators: HashMap<Type, CacheOperator<*>>
) {

    class Builder(private val context: Context) {

        private var cacheDir: File = Files.defaultCacheDir(context)
        private var maxMemoryCount = DEFAULT_MEMORY_COUNT
        private val cacheOperators = HashMap<Type, CacheOperator<*>>()
        private var diskTrimStrategy: LruDiskTrim.Strategy? = null

        fun cacheDir(cacheDir: File): Builder {
            this.cacheDir = cacheDir
            return this
        }

        fun maxMemoryCount(maxCount: Int): Builder {
            this.maxMemoryCount = maxCount
            return this
        }

        fun maxDiskCount(maxCount: Int): Builder {
            if (diskTrimStrategy != null) {
                logw("Override old diskTrimStrategy")
            }
            diskTrimStrategy = LruDiskTrim.CountStrategy(maxCount)
            return this
        }

        fun maxDiskSize(maxSize: Long): Builder {
            if (diskTrimStrategy != null) {
                logw("Override old diskTrimStrategy")
            }
            diskTrimStrategy = LruDiskTrim.SizeStrategy(maxSize)
            return this
        }

        fun addCacheOperator(type: Type, cacheOperator: CacheOperator<*>): Builder {
            this.cacheOperators[type] = cacheOperator
            return this
        }

        fun debug(debug: Boolean): Builder {
            ObjCache.debug(debug)
            return this
        }

        fun create(): ObjCache {
            return ObjCache(
                context,
                cacheDir,
                maxMemoryCount,
                diskTrimStrategy ?: LruDiskTrim.LazyStrategy(),
                cacheOperators
            )
        }
    }

    internal val staticCacheOperatorManager = CacheOperatorManager()
    internal val cacheDispatcher = ObjCacheDispatcher(this)
    internal val lruDiskTrim: LruDiskTrim

    init {
        default = this

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        } else if (!cacheDir.isDirectory) {
            throw IllegalArgumentException("cacheDir必须是文件夹: " + cacheDir.path)
        }

        lruDiskTrim = LruDiskTrim(diskTrimStrategy)

        staticCacheOperatorManager.register(Int::class.java, SpOperator.Int())
        staticCacheOperatorManager.register(Long::class.java, SpOperator.Long())
        staticCacheOperatorManager.register(Float::class.java, SpOperator.Float())
        staticCacheOperatorManager.register(Boolean::class.java, SpOperator.Boolean())
        staticCacheOperatorManager.register(String::class.java, SpOperator.String())
        staticCacheOperatorManager.register(
            Parcelable::class.java,
            ParcelableOperator<Parcelable>()// 只提供了序列化操作，反序列化会抛出异常
        )
        staticCacheOperatorManager.register(Serializable::class.java, SerializableOperator())

        for (entry in cacheOperators) {
            staticCacheOperatorManager.register(entry.key, entry.value)
        }
    }

    companion object {

        private const val DEFAULT_MEMORY_COUNT = 1000
        internal const val LOG_T = "ObjCache"

        internal var defaultContext: Context? = null
        private var default: ObjCache? = null

        private fun createDefault(context: Context): ObjCache {
            return Builder(context)
                .create()
        }

        fun debug(debug: Boolean) {
            Utils.debug = debug
        }

        @JvmStatic
        fun default(): ObjCache {
            return default ?: createDefault(checkNotNull(defaultContext) {
                "Context == null"
            })
        }

        @JvmStatic
        fun with(type: Type): RequestBuilder {
            return RequestBuilder(type)
        }

        @JvmStatic
        fun with(operator: CacheOperator<*>): RequestBuilder {
            return RequestBuilder(operator)
        }

        @JvmStatic
        fun clear() {
            default().cacheDispatcher.clear()
        }
    }
}