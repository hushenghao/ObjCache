package com.che300.objcache.annotation

import com.che300.objcache.operator.CacheOperator

/**
 * 生成缓存key的因子
 *
 * @see CacheOperator
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KeyFactor(val keyFactor: String)
