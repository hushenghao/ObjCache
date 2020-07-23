package com.che300.objcache.annotation

import com.che300.objcache.cache.CacheStrategy
import com.che300.objcache.operator.CacheOperator
import com.che300.objcache.operator.SpOperator

/**
 * CacheOperator使用的缓存类型
 *
 * @property strategy 使用的缓存策略
 * @property defaultFile 是否使用CacheKey提供的文件进行缓存存储
 *
 * @see CacheOperator
 * @see SpOperator
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class OperatorStrategy(val strategy: CacheStrategy, val defaultFile: Boolean = true)