package com.che300.objcache.operator

import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.util.Utils

/**
 * 缓存序列化与反序列化接口
 */
@KeyFactor(keyFactor = "cache")
interface CacheOperator<T> {

    fun get(key: CacheKey, default: T?): T?

    fun put(key: CacheKey, value: T): Boolean

    fun remove(key: CacheKey): Boolean {
        val cacheFile = key.cacheFile()
        if (!cacheFile.exists()) {
            return true
        }
        return cacheFile.delete()
    }
}

/**
 * 获取注解声明的序列化key生成因子
 */
internal fun CacheOperator<*>.keyFactor(): String {
    val annotation = Utils.getKeyFactor(this.javaClass)
    return annotation?.keyFactor ?: ""
}

/**
 * 获取缓存序列化与反序列化实例的缓存策略, Sp默认跳过内存缓存[SpOperator]
 */
internal fun CacheOperator<*>.operatorStrategy(): CacheStrategy {
    val annotation = Utils.getOperatorStrategy(this.javaClass)
    return annotation?.strategy ?: CacheStrategy.ALL
}