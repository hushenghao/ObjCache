package com.che300.objcache.operator

import com.che300.objcache.util.Utils
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.cache.CacheStrategy

/**
 * 缓存序列化与反序列化接口
 */
@KeyFactor(keyFactor = "cache")
interface CacheOperator<T> {

    fun get(key: CacheKey, default: T?): T?

    fun put(key: CacheKey, value: T?): Boolean

}

internal fun CacheOperator<*>.keyFactor(): String {
    val annotation = Utils.getKeyFactor(this.javaClass)
    return annotation?.keyFactor ?: ""
}

@com.che300.objcache.annotation.CacheStrategy
internal fun CacheOperator<*>.operatorStrategy(): Int {
    val annotation = Utils.getOperatorStrategy(this.javaClass)
    return annotation?.strategy ?: CacheStrategy.ALL
}