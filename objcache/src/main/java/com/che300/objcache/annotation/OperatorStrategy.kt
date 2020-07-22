package com.che300.objcache.annotation

import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.operator.CacheOperator
import com.che300.objcache.operator.SpOperator

/**
 * CacheOperator使用的缓存类型
 *
 * @see CacheOperator
 * @see SpOperator
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class OperatorStrategy(val strategy: CacheStrategy)