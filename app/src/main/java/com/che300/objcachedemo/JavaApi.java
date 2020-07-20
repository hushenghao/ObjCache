package com.che300.objcachedemo;

import com.che300.objcache.ObjCache;
import com.che300.objcache.ObjCacheKt;
import com.che300.objcache.cache.CacheStrategy;

/**
 * @author hsh
 * @since 2020/7/20 3:28 PM
 */
class JavaApi {

    public static void main(String[] args) {
        ObjCacheKt.getString(ObjCache.Companion, "", "");
        ObjCache.with(String.class)
                .cacheStrategy(CacheStrategy.DISK)
                .get("","");
    }
}
