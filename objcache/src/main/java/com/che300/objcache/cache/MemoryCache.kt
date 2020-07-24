package com.che300.objcache.cache

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import androidx.collection.LruCache
import com.che300.objcache.util.log

/**
 * 内存缓存
 */
internal class MemoryCache(maxSize: Int) : ComponentCallbacks2 {

    private val lruCache = object : LruCache<CacheKey, Any>(maxSize) {
        override fun trimToSize(maxSize: Int) {
            super.trimToSize(maxSize)
            if (this.maxSize() > maxSize) {
                log("TRIM memory cache: $maxSize")
            }
        }
    }

    fun put(cacheKey: CacheKey, any: Any) {
        lruCache.put(cacheKey, any)
        log("PUT $cacheKey: memory")
    }

    fun remove(cacheKey: CacheKey) {
        lruCache.remove(cacheKey)
        log("REMOVE $cacheKey: memory")
    }

    fun get(cacheKey: CacheKey): Any? {
        val get = lruCache.get(cacheKey)
        log("GET $cacheKey: memory")
        return get
    }

    fun clear() {
        lruCache.trimToSize(0)
    }

    override fun onLowMemory() {
        clear()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
    }

    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            // Nearing middle of list of cached background apps
            // Evict our entire obj cache
            clear()
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            // Entering list of cached background apps
            // Evict oldest half of our obj cache
            lruCache.trimToSize(lruCache.size() / 2)
        }
    }
}