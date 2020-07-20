package com.che300.objcache.annotation

/**
 * CacheOperator需要的缓存类型
 * @see com.che300.objcache.operator.CacheOperator
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class OperatorStrategy(@com.che300.objcache.annotation.CacheStrategy val strategy: Int)