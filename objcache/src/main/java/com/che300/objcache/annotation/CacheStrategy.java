package com.che300.objcache.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 缓存类型校验注解
 */
@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@IntDef({com.che300.objcache.cache.CacheStrategy.DISK,
        com.che300.objcache.cache.CacheStrategy.MEMORY,
        com.che300.objcache.cache.CacheStrategy.ALL,
        com.che300.objcache.cache.CacheStrategy.NONE})
public @interface CacheStrategy {
}
