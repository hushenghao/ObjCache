package com.che300.objcache.util

import android.util.Log
import com.che300.objcache.ObjCache
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.annotation.OperatorStrategy

internal fun log(msg: String) {
    if (Utils.isDebug()) {
        Log.d(ObjCache.LOG_T, msg)
    }
}

internal fun logw(msg: String) {
    if (Utils.isDebug()) {
        Log.w(ObjCache.LOG_T, msg)
    }
}

internal object Utils {

    internal var debug = false

    fun isDebug(): Boolean = debug

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

}