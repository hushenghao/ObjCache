package com.che300.objcache.cache

import android.app.Application
import android.os.Looper
import android.os.SystemClock
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

    private val lock = Any()

    private val cacheExecutor = Executors.newCachedThreadPool()
    private val memoryCacheManager = MemoryCache(objCache.maxMemoryCount)

    init {
        val application = objCache.appContext as? Application
        application?.registerComponentCallbacks(memoryCacheManager)
    }

    private class Clear : Runnable {
        override fun run() {
            val cacheDir = ObjCache.default().cacheDir
            val listFiles = cacheDir.listFiles() ?: return
            for (file in listFiles) {
                synchronized(Clear::class) {
                    if (!file.exists()) {
                        return@synchronized
                    }
                    file.delete()
                }
            }
        }
    }

    internal fun clear() {
        log("CLEAR all cache")
        memoryCacheManager.clear()
        cacheExecutor.execute(Clear())
        SpOperator.clear()
    }

    internal fun remove(
        request: RequestBuilder<*>,
        operator: CacheOperator<*>
    ): Boolean {
        val cacheKey = CacheKey.create(request.key, operator.keyFactor())
        log("REMOVE $cacheKey: operator " + operator.javaClass.name)
        val operatorStrategy = operator.operatorStrategy()
        val strategy = request.strategy and operatorStrategy.strategy
        if (strategy.hasStrategy(CacheStrategy.MEMORY)) {
            memoryCacheManager.remove(cacheKey)
        } else {
            log("REMOVE $cacheKey: skip memory")
        }
        if (strategy.hasStrategy(CacheStrategy.DISK)) {
            val future = cacheExecutor.submit(Callable<Boolean> {
                synchronized(lock) {
                    if (operatorStrategy.defaultFile) {
                        if (!cacheKey.exists()) {
                            return@Callable true
                        }
                    } else {
                        log("REMOVE $cacheKey: uncheck disk file")
                    }
                    var result = false
                    try {
                        result = operator.remove(cacheKey)
                    } catch (e: IOException) {
                        logw("REMOVE $cacheKey error: ${e.localizedMessage}")
                    }
                    return@Callable result
                }
            })
            if (future.isDone) {
                val get = future.get()
                log("REMOVE $cacheKey: disk $get")
                return get
            } else {
                log("REMOVE $cacheKey: disk")
            }
        } else {
            log("REMOVE $cacheKey: skip disk")
        }
        return true
    }

    internal fun <T> put(
        request: RequestBuilder<T>,
        value: T,
        operator: CacheOperator<T>
    ): Boolean {
        val cacheKey = CacheKey.create(request.key, operator.keyFactor())
        log("PUT $cacheKey: operator " + operator.javaClass.name)
        val operatorStrategy = operator.operatorStrategy()
        val strategy = request.strategy and operatorStrategy.strategy

        if (strategy.hasStrategy(CacheStrategy.MEMORY)) {
            val any = value as Any
            memoryCacheManager.put(cacheKey, any)
        } else {
            log("PUT $cacheKey: skip memory")
        }

        if (strategy.hasStrategy(CacheStrategy.DISK)) {
            val future = cacheExecutor.submit(Callable<Boolean> {
                synchronized(lock) {
                    if (operatorStrategy.defaultFile) {
                        if (!cacheKey.exists()) {
                            cacheKey.cacheFile().createNewFile()
                        }
                    } else {
                        log("PUT $cacheKey: uncheck disk file")
                    }
                    var result = false
                    try {
                        result = operator.put(cacheKey, value)
                    } catch (e: IOException) {
                        logw("PUT $cacheKey error: ${e.localizedMessage}")
                    }

                    setLastModifiedNow(cacheKey)

                    trimDiskCache()

                    return@Callable result
                }
            })
            if (future.isDone) {
                val get = future.get()
                log("PUT $cacheKey: disk $get")
                return get
            } else {
                log("PUT $cacheKey: disk")
            }
        } else {
            log("PUT $cacheKey: skip disk")
        }
        return true
    }

    internal fun <T> get(
        request: RequestBuilder<T>,
        default: T?,
        operator: CacheOperator<T>
    ): T? {
        val cacheKey = CacheKey.create(request.key, operator.keyFactor())
        log("GET $cacheKey: operator " + operator.javaClass.name)
        val operatorStrategy = operator.operatorStrategy()
        val strategy = request.strategy and operatorStrategy.strategy

        var result: T? = null
        val hasMemory = strategy.hasStrategy(CacheStrategy.MEMORY)
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

        if (strategy.hasStrategy(CacheStrategy.DISK)) {
            val future = cacheExecutor.submit(Callable<T> {
                synchronized(lock) {
                    if (operatorStrategy.defaultFile) {
                        if (!cacheKey.exists()) {
                            return@Callable default
                        }
                    } else {
                        log("GET $cacheKey: uncheck disk file")
                    }
                    val get: T? = try {
                        operator.get(cacheKey, default)
                    } catch (e: IOException) {
                        logw("GET $cacheKey error: ${e.localizedMessage}")
                        null
                    }

                    setLastModifiedNow(cacheKey)
                    return@Callable get
                }
            })
            log("GET $cacheKey: disk")
            val s = SystemClock.uptimeMillis()
            // may be block UI Thread
            var cache: T? = null
            try {
                cache = future.get()
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
                val myLooper = Looper.myLooper()
                if (myLooper != null && myLooper == Looper.getMainLooper()) {
                    val total = SystemClock.uptimeMillis() - s
                    if (total > 16) {
                        logw("The main thread is blocked. ${total}ms")
                    }
                }
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

    /**
     * 更新文件修改时间
     */
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