package com.che300.objcache

import android.content.Context
import android.util.Log
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.annotation.OperatorStrategy
import com.che300.objcache.cache.CacheKey
import okio.buffer
import okio.sink
import okio.source
import java.io.Closeable
import java.io.File
import java.io.IOException

internal fun Any.log(msg: String) {
    if (Utils.debug) {
        Log.d(ObjCache.LOG_T, msg)
    }
}

internal fun Closeable?.safeClose() {
    this ?: return
    try {
        this.close()
    } catch (e: IOException) {
    }
}

internal object Utils {

    internal var debug = false

    fun getKeyFactor(clazz: Class<*>?): KeyFactor? {
        if (clazz == null) {
            return null
        }
        val annotation = clazz.getAnnotation(KeyFactor::class.java)
        if (annotation != null) {
            return annotation
        }
        return getKeyFactor(clazz.superclass)
    }

    fun getOperatorStrategy(clazz: Class<*>?): OperatorStrategy? {
        if (clazz == null) {
            return null
        }
        val annotation = clazz.getAnnotation(OperatorStrategy::class.java)
        if (annotation != null) {
            return annotation
        }
        return getOperatorStrategy(clazz.superclass)
    }

    /**
     * 默认缓存路径
     */
    internal fun defaultCacheDir(context: Context): File {
        val file = File(context.externalCacheDir ?: context.cacheDir, "ObjCache")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    internal fun readUtf8(cacheFile: File): String? {
        if (!cacheFile.exists()) {
            return null
        }
        val buffer = cacheFile.source().buffer()
        val readUtf8 = buffer.readUtf8()
        buffer.close()
        return readUtf8
    }

    internal fun readBytes(cacheFile: File): ByteArray? {
        if (!cacheFile.exists()) {
            return null
        }
        val buffer = cacheFile.source().buffer()
        val byteArray = buffer.readByteArray()
        buffer.close()
        return byteArray
    }

    internal fun writeUtf8(cacheFile: File, utf8: String) {
        cacheFile.sink()
            .buffer()
            .writeUtf8(utf8)
            .close()
    }

    internal fun writeBytes(cacheFile: File, byteArray: ByteArray) {
        cacheFile.sink()
            .buffer()
            .write(byteArray)
            .close()
    }

}