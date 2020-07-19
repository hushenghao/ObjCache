package com.che300.objcache.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KeyFactor(val keyFactor: String)
