package com.che300.objcache.cache

import androidx.collection.LruCache
import com.che300.objcache.log

internal class MemoryCacheManager(private val maxSize: Int) {

    private var lruCache = LruCache<CacheKey, Any>(maxSize)

    fun put(cacheKey: CacheKey, any: Any?) {
        if (any == null) {
            lruCache.remove(cacheKey)
            log("DEL $cacheKey: memory")
            return
        }
        lruCache.put(cacheKey, any)
        log("PUT $cacheKey: memory")
    }

    fun get(cacheKey: CacheKey): Any? {
        val get = lruCache.get(cacheKey)
        log("GET $cacheKey: memory")
        return get
    }

    fun clear() {
        lruCache = LruCache(maxSize)
    }
}