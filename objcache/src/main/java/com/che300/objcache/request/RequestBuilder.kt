package com.che300.objcache.request

import com.che300.objcache.ObjCache
import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.cache.ObjCacheDispatcher
import com.che300.objcache.operator.CacheOperator
import com.che300.objcache.util.log
import java.lang.reflect.Type

/**
 * @author hsh
 * @since 2020/7/20 11:42 AM
 */
open class RequestBuilder<T> {

    internal lateinit var key: String

    @com.che300.objcache.annotation.CacheStrategy
    internal var strategy: Int = CacheStrategy.ALL

    @PublishedApi
    internal var operator: CacheOperator<*>

    constructor(type: Type) {
        operator = ObjCache.default().staticCacheOperatorManager.get<Any>(type)
    }

    constructor(operator: CacheOperator<T>) {
        this.operator = operator
    }


    open fun cacheStrategy(@com.che300.objcache.annotation.CacheStrategy strategy: Int): RequestBuilder<T> {
        this.strategy = strategy
        return this
    }

    private fun cacheDispatcher(): ObjCacheDispatcher {
        return ObjCache.default().cacheDispatcher
    }

    fun get(key: String, default: T?): T? {
        log("GET ($key) operator: " + operator.javaClass.name)
        val operator = this.operator as CacheOperator<T>
        this.key = key
        return cacheDispatcher().get<T>(this, default, operator)
    }

    fun put(key: String, value: T?): Boolean {
        log("PUT ($key) operator: " + operator.javaClass.name)
        val operator = this.operator as CacheOperator<T>
        this.key = key
        return cacheDispatcher().put<T>(this, value, operator)
    }

}