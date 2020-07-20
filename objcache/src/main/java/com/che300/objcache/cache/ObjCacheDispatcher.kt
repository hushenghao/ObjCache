package com.che300.objcache.cache

import com.che300.objcache.ObjCache
import com.che300.objcache.operator.CacheOperator
import com.che300.objcache.operator.SpOperator
import com.che300.objcache.operator.keyFactor
import com.che300.objcache.operator.operatorStrategy
import com.che300.objcache.request.RequestBuilder
import com.che300.objcache.util.Files
import com.che300.objcache.util.log
import com.che300.objcache.util.logw
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

/**
 * 缓存调度
 */
internal class ObjCacheDispatcher(private val objCache: ObjCache) {

    private val cacheExecutor = Executors.newCachedThreadPool()
    private val memoryCacheManager = MemoryCacheManager(objCache.maxMemoryCount)

    private class Clear : Runnable {
        override fun run() {
            val cacheDir = ObjCache.default().cacheDir
            val listFiles = cacheDir.listFiles() ?: return
            for (file in listFiles) {
                file.delete()
            }
        }
    }

    internal fun clear() {
        log("DEL all cache")
        memoryCacheManager.clear()
        cacheExecutor.execute(Clear())
        SpOperator.clear()
    }

    internal fun <T> put(
        request: RequestBuilder,
        value: T?,
        operator: CacheOperator<T>
    ): Boolean {
        val key = request.key
        val strategy = request.strategy and operator.operatorStrategy()
        val cacheKey = CacheKey(key, operator.keyFactor())

        if (CacheStrategy.hasStrategy(strategy, CacheStrategy.MEMORY)) {
            memoryCacheManager.put(cacheKey, value)
        } else {
            log("PUT $cacheKey: skip memory")
        }

        if (CacheStrategy.hasStrategy(strategy, CacheStrategy.DISK)) {
            cacheExecutor.execute {
                synchronized(this) {
                    operator.put(cacheKey, value)

                    setLastModifiedNow(cacheKey)

                    trimDiskCache()
                }
            }
            log("PUT $cacheKey: disk")
        } else {
            log("PUT $cacheKey: skip disk")
        }
        return true
    }

    internal fun <T> get(
        request: RequestBuilder,
        default: T?,
        operator: CacheOperator<T>
    ): T? {
        val key = request.key
        val strategy = request.strategy and operator.operatorStrategy()
        val cacheKey = CacheKey(key, operator.keyFactor())

        var result: T? = null
        val hasMemory = CacheStrategy.hasStrategy(strategy, CacheStrategy.MEMORY)
        if (hasMemory) {
            val memoryCache = memoryCacheManager.get(cacheKey) as? T
            if (memoryCache != null) {
                result = memoryCache
            } else {
                log("GET $cacheKey: memory not found")
            }
        } else {
            log("GET $cacheKey: skip memory")
        }
        if (result != null) {
            return result
        }

        if (CacheStrategy.hasStrategy(strategy, CacheStrategy.DISK)) {
            log("GET $cacheKey: disk")
            val submit = cacheExecutor.submit(Callable<T> {
                synchronized(this) {
                    val get = operator.get(cacheKey, default)

                    setLastModifiedNow(cacheKey)
                    return@Callable get
                }
            })
            // may be block UI Thread
            var cache: T? = null
            try {
                cache = submit.get()
            } catch (e: CancellationException) {
                logw("GET $cacheKey: ${e.localizedMessage}")
            } catch (e: InterruptedException) {
                logw("GET $cacheKey: ${e.localizedMessage}")
            } catch (e: ExecutionException) {
                val cause = e.cause
                if (cause is RuntimeException) {
                    throw cause
                }
                logw("GET $cacheKey: ${e.localizedMessage}")
            } finally {
                if (cache != null && hasMemory) {
                    memoryCacheManager.put(cacheKey, cache)
                } else if (!hasMemory) {
                    log("PUT $cacheKey: skip memory")
                }
            }
            result = cache
        } else {
            log("GET $cacheKey: skip disk")
        }
        if (result != null) {
            return result
        }
        log("GET $cacheKey: cache not found")
        return default
    }

    private fun setLastModifiedNow(key: CacheKey) {
        val cacheFile = key.cacheFile()
        try {
            Files.setLastModifiedNow(cacheFile)
        } catch (e: IOException) {
            logw("setLastModified error: $cacheFile")
        }
    }

    /**
     * 删除过期磁盘缓存
     */
    private fun trimDiskCache() {
        val cacheDir = objCache.cacheDir
        val trimCallable = objCache.lruDiskTrim.TrimCallable(cacheDir)
        cacheExecutor.submit(trimCallable)
    }

}