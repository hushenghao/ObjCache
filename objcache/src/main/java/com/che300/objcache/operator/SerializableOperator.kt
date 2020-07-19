package com.che300.objcache.operator

import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.safeClose
import java.io.*

@KeyFactor("Serializable")
class SerializableOperator : CacheOperator<Serializable> {

    override fun get(key: CacheKey, default: Serializable?): Serializable? {
        val cacheFile = key.cacheFile()
        if (!cacheFile.exists()) {
            return default
        }
        var ois: ObjectInputStream? = null
        try {
            ois = ObjectInputStream(FileInputStream(cacheFile))
            return (ois.readObject() as? Serializable) ?: default
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ois.safeClose()
        }
        return default
    }

    override fun put(key: CacheKey, value: Serializable?): Boolean {
        val cacheFile = key.cacheFile()
        if (value == null) {
            return cacheFile.delete()
        }

        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(FileOutputStream(cacheFile))
            oos.writeObject(value)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            oos.safeClose()
        }
        return true
    }

}
