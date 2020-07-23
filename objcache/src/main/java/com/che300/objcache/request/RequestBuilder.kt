package com.che300.objcache.request

import com.che300.objcache.ObjCache
import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.cache.ObjCacheDispatcher
import com.che300.objcache.operator.CacheOperator
import java.lang.reflect.Type

/**
 * 缓存请求构建
 */
open class RequestBuilder<T> {

    internal lateinit var key: String

    internal var strategy: CacheStrategy = CacheStrategy.ALL

    @PublishedApi
    internal var operator: CacheOperator<*>

    constructor(type: Type) {
        operator = ObjCache.default().staticCacheOperatorManager.get<Any>(type)
    }

    constructor(operator: CacheOperator<T>) {
        this.operator = operator
    }


    open fun cacheStrategy(strategy: CacheStrategy): RequestBuilder<T> {
        this.strategy = strategy
        return this
    }

    private fun cacheDispatcher(): ObjCacheDispatcher {
        return ObjCache.default().cacheDispatcher
    }

    fun get(key: String, default: T?): T? {
        val operator = this.operator as CacheOperator<T>
        this.key = key
        return cacheDispatcher().get<T>(this, default, operator)
    }

    fun put(key: String, value: T): Boolean {
        val operator = this.operator as CacheOperator<T>
        this.key = key
        return cacheDispatcher().put<T>(this, value, operator)
    }

    fun remove(key: String): Boolean {
        val operator = this.operator
        this.key = key
        return cacheDispatcher().remove(this, operator)
    }
}