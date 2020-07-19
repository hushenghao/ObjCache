package com.che300.objcache.cache

import com.che300.objcache.ObjCache
import com.che300.objcache.log
import com.che300.objcache.operator.CacheOperator
import com.che300.objcache.operator.SpOperator
import com.che300.objcache.operator.keyFactor
import com.che300.objcache.operator.operatorStrategy
import com.che300.objcache.request.EditRequest
import com.che300.objcache.request.GetRequest
import java.lang.reflect.Type
import java.util.concurrent.Callable
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
        log("DEL all")
        memoryCacheManager.clear()
        cacheExecutor.execute(Clear())
        SpOperator.clear()
    }

    internal fun <T> put(
        request: EditRequest,
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
                }
            }
            log("PUT $cacheKey: disk")
        } else {
            log("PUT $cacheKey: skip disk")
        }
        return true
    }

    internal fun <T> put(request: EditRequest, type: Type, value: T?): Boolean {
        val operator = objCache.staticCacheOperatorManager.get<T>(type)
        return put(request, value, operator)
    }


    internal fun <T> get(request: GetRequest, type: Type, default: T?): T? {
        val operator = objCache.staticCacheOperatorManager.get<T>(type)
        return get(request, default, operator)
    }

    internal fun <T> get(
        request: GetRequest,
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
                    return@Callable operator.get(cacheKey, default)
                }
            })
            // may be block UI Thread
            val cache = try {
                submit.get()
            } catch (e: Exception) {
                null
            }
            if (cache != null && hasMemory) {
                memoryCacheManager.put(cacheKey, cache)
            } else if (!hasMemory) {
                log("PUT $cacheKey: skip memory")
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

}