package com.che300.objcache.operator

import com.che300.objcache.Utils
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import org.json.JSONException
import org.json.JSONObject

@KeyFactor("JSONObject")
class JSONObjectOperator : CacheOperator<JSONObject> {

    override fun get(key: CacheKey, default: JSONObject?): JSONObject? {
        val cacheFile = key.cacheFile()
        try {
            return JSONObject(Utils.readUtf8(cacheFile))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return default
    }

    override fun put(key: CacheKey, value: JSONObject?): Boolean {
        val cacheFile = key.cacheFile()
        if (value == null) {
            return cacheFile.delete()
        }
        val toString = value.toString()
        Utils.writeUtf8(cacheFile, toString)
        return true
    }
}