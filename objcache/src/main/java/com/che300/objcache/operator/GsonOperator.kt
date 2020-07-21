package com.che300.objcache.operator

import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.util.Files
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

/**
 * Gson全类型缓存操作者
 */
@KeyFactor("Gson")
internal class GsonOperator<T>(private val type: Type) : CacheOperator<T> {

    override fun get(key: CacheKey, default: T?): T? {
        val cacheFile = key.cacheFile()
        val readUtf8 = Files.readUtf8(cacheFile)
        try {
            return Gson().fromJson(readUtf8, type)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
        return null
    }

    override fun put(key: CacheKey, value: T?): Boolean {
        val cacheFile = key.cacheFile()
        if (value == null) {
            return cacheFile.delete()
        }
        val json = Gson().toJson(value, type)
        Files.writeUtf8(cacheFile, json)
        return true
    }
}