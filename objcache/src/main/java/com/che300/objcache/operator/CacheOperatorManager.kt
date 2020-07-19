package com.che300.objcache.operator

import java.lang.reflect.Type

class CacheOperatorManager {

    private val cacheOperator = HashMap<Type, CacheOperator<*>>()

    fun <T> register(type: Type, operator: CacheOperator<T>) {
        cacheOperator[type] = operator
    }

    fun <T> get(type: Type): CacheOperator<T> {
        val cacheOperator = cacheOperator[type] as? CacheOperator<T>
        return cacheOperator ?: GsonOperator<T>(type)
    }
}