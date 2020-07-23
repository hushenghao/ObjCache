package com.che300.objcache.cache

import com.che300.objcache.ObjCache
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.operator.CacheOperator
import java.io.File
import java.util.*

/**
 * 缓存key
 * @property key 字符串key入参
 * @property factor 对象序列化类型的因子. [CacheOperator],[KeyFactor]
 */
data class CacheKey(val key: String, val factor: String = "") {

    companion object {
        internal fun create(key: String, factor: String): CacheKey {
            return CacheKey(key, factor)
        }
    }

    /**
     * 获取缓存文件对象
     */
    fun cacheFile(): File {
        return File(ObjCache.default().cacheDir, cacheFileName())
    }

    /**
     * 磁盘缓存文件名
     */
    fun cacheFileName(): String {
        val name = "$key:$factor"
        if (!ObjCache.diskCacheEncode) {
            return name
        }
        return UUID.nameUUIDFromBytes(name.toByteArray()).toString()
    }

    override fun toString(): String {
        return "($key:$factor)"
    }
}

internal fun CacheKey.exists(): Boolean {
    return cacheFile().exists()
}