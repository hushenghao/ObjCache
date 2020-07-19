package com.che300.objcache.operator

import com.che300.objcache.Utils
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import org.json.JSONArray
import org.json.JSONException

@KeyFactor("JSONArray")
class JSONArrayOperator : CacheOperator<JSONArray> {

    override fun get(key: CacheKey, default: JSONArray?): JSONArray? {
        val cacheFile = key.cacheFile()
        try {
            return JSONArray(Utils.readUtf8(cacheFile))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return default
    }

    override fun put(key: CacheKey, value: JSONArray?): Boolean {
        val cacheFile = key.cacheFile()
        if (value == null) {
            return cacheFile.delete()
        }
        val toString = value.toString()
        Utils.writeUtf8(cacheFile, toString)
        return true
    }
}