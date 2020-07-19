package com.che300.objcache.cache

import com.che300.objcache.ObjCache
import java.io.File
import java.util.*

data class CacheKey(val key: String, val factor: String = "") {

    /**
     * 获取缓存文件对象
     */
    fun cacheFile(): File {
        return File(ObjCache.default().cacheDir, cacheName())
    }

    /**
     * 磁盘缓存文件名
     */
    fun cacheName(): String {
        return UUID.nameUUIDFromBytes((key + factor).toByteArray()).toString()
    }

    override fun toString(): String {
        return "($key:$factor)"
    }
}