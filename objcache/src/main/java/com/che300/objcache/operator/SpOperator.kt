package com.che300.objcache.operator

import android.content.Context
import android.content.SharedPreferences
import com.che300.objcache.ObjCache
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.annotation.OperatorStrategy
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.cache.CacheStrategy

/**
 * 默认Sp缓存支持
 *
 * 默认跳过了内存缓存
 */
@KeyFactor("SP")
@OperatorStrategy(strategy = CacheStrategy.DISK_UNCHECK_FILE)// 忽略文件状态标志
internal abstract class SpOperator<T> : CacheOperator<T> {

    companion object {
        protected val sp: SharedPreferences =
            ObjCache.default().context.getSharedPreferences("obj_cache", Context.MODE_PRIVATE)

        fun clear() {
            sp.edit().clear().apply()
        }
    }

    override fun remove(key: CacheKey): Boolean {
        sp.edit().remove(key.key).apply()
        return true
    }

    internal class _Int : SpOperator<Int>() {
        override fun get(key: CacheKey, default: Int?): Int? {
            return sp.getInt(key.key, default ?: 0)
        }

        override fun put(key: CacheKey, value: Int): Boolean {
            sp.edit().putInt(key.key, value).apply()
            return true
        }
    }

    internal class _Long : SpOperator<Long>() {
        override fun get(key: CacheKey, default: Long?): Long? {
            return sp.getLong(key.key, default ?: 0L)
        }

        override fun put(key: CacheKey, value: Long): Boolean {
            sp.edit().putLong(key.key, value).apply()
            return true
        }
    }

    internal class _Float : SpOperator<Float>() {
        override fun get(key: CacheKey, default: Float?): Float? {
            return sp.getFloat(key.key, default ?: 0f)
        }

        override fun put(key: CacheKey, value: Float): Boolean {
            sp.edit().putFloat(key.key, value).apply()
            return true
        }
    }

    internal class _String : SpOperator<String>() {
        override fun get(key: CacheKey, default: String?): String? {
            return sp.getString(key.key, default)
        }

        override fun put(key: CacheKey, value: String): Boolean {
            sp.edit().putString(key.key, value).apply()
            return true
        }
    }

    internal class _Boolean : SpOperator<Boolean>() {
        override fun get(key: CacheKey, default: Boolean?): Boolean? {
            return sp.getBoolean(key.key, default ?: false)
        }

        override fun put(key: CacheKey, value: Boolean): Boolean {
            sp.edit().putBoolean(key.key, value).apply()
            return true
        }
    }
}