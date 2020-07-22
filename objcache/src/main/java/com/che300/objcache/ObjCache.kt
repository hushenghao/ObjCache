package com.che300.objcache

import android.content.Context
import android.os.Bundle
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

        /**
         * 缓存文件夹
         */
        fun cacheDir(cacheDir: File): Builder {
            this.cacheDir = cacheDir
            return this
        }

        /**
         * 最大内存缓存数量
         */
        fun maxMemoryCount(maxCount: Int): Builder {
            this.maxMemoryCount = maxCount
            return this
        }

        /**
         * 最大磁盘缓存数量
         */
        fun maxDiskCount(maxCount: Int): Builder {
            if (diskTrimStrategy != null) {
                logw("Override old diskTrimStrategy")
            }
            diskTrimStrategy = LruDiskTrim.CountStrategy(maxCount)
            return this
        }

        /**
         * 最大磁盘缓存大小
         */
        fun maxDiskSize(maxSize: Long): Builder {
            if (diskTrimStrategy != null) {
                logw("Override old diskTrimStrategy")
            }
            diskTrimStrategy = LruDiskTrim.SizeStrategy(maxSize)
            return this
        }

        /**
         * 自定义类型缓存操作者
         */
        fun addCacheOperator(type: Type, cacheOperator: CacheOperator<*>): Builder {
            this.cacheOperators[type] = cacheOperator
            return this
        }

        /**
         * 日志
         */
        fun debug(debug: Boolean): Builder {
            ObjCache.debug(debug)
            return this
        }

        /**
         * 磁盘缓存文件名是否进行编码，默认不编码
         */
        fun diskCacheKeyEncode(encode: Boolean): Builder {
            ObjCache.diskCacheEncode = encode
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

        staticCacheOperatorManager.register(Int::class.java, SpOperator._Int())
        staticCacheOperatorManager.register(Long::class.java, SpOperator._Long())
        staticCacheOperatorManager.register(Float::class.java, SpOperator._Float())
        staticCacheOperatorManager.register(Boolean::class.java, SpOperator._Boolean())
        staticCacheOperatorManager.register(String::class.java, SpOperator._String())
        staticCacheOperatorManager.register(
            Parcelable::class.java,
            ParcelableOperator<Parcelable>()// 只提供了序列化操作，反序列化会抛出异常
        )
        staticCacheOperatorManager.register(
            Serializable::class.java,
            SerializableOperator.Fast()
        )
        staticCacheOperatorManager.register(Bundle::class.java, BundleOperator())

        // 注册自定义类型解析，会覆盖默认解析器
        for (entry in cacheOperators) {
            staticCacheOperatorManager.register(entry.key, entry.value)
        }
    }

    companion object {

        // 默认内存缓存数量
        private const val DEFAULT_MEMORY_COUNT = 1000
        internal const val LOG_T = "ObjCache"

        internal var defaultContext: Context? = null
        private var default: ObjCache? = null

        // 磁盘缓存是否进行编码
        internal var diskCacheEncode = false

        private fun createDefault(context: Context): ObjCache {
            return Builder(context)
                .create()
        }

        fun debug(debug: Boolean) {
            Utils.debug = debug
        }

        /**
         * 默认实例
         */
        @JvmStatic
        fun default(): ObjCache {
            return default ?: createDefault(checkNotNull(defaultContext) {
                "Context == null"
            })
        }

        @JvmStatic
        fun <T> with(type: Type): RequestBuilder<T> {
            return RequestBuilder(type)
        }

        @JvmStatic
        fun <T> with(clazz: Class<T>): RequestBuilder<T> {
            return RequestBuilder(clazz as Type)
        }

        @JvmStatic
        fun <T> with(operator: CacheOperator<T>): RequestBuilder<T> {
            return RequestBuilder(operator)
        }

        /**
         * 清空所有缓存
         */
        @JvmStatic
        fun clear() {
            default().cacheDispatcher.clear()
        }
    }
}