package com.che300.objcache.operator

import android.content.Context
import android.content.SharedPreferences
import com.che300.objcache.ObjCache
import com.che300.objcache.annotation.KeyFactor
import com.che300.objcache.annotation.OperatorStrategy
import com.che300.objcache.cache.CacheKey
import com.che300.objcache.cache.CacheStrategy
import java.lang.Character.MIN_VALUE

/**
 * 默认Sp缓存支持
 *
 * 默认跳过了内存缓存
 */
@KeyFactor("SP")
@OperatorStrategy(strategy = CacheStrategy.DISK)
internal abstract class SpOperator<T> : CacheOperator<T> {

    companion object {
        protected val sp: SharedPreferences =
            ObjCache.default().context.getSharedPreferences("obj_cache", Context.MODE_PRIVATE)

        fun clear() {
            sp.edit().clear().apply()
        }
    }

    protected fun <T> remove(key: CacheKey, t: T?): kotlin.Boolean {
        if (t == null) {
            sp.edit().remove(key.key).apply()
            return true
        }
        return false
    }

    internal class Char : SpOperator<kotlin.Char>() {
        override fun get(key: CacheKey, default: kotlin.Char?): kotlin.Char? {
            return sp.getInt(key.key, (default ?: MIN_VALUE).toInt()).toChar()
        }

        override fun put(key: CacheKey, value: kotlin.Char?): kotlin.Boolean {
            if (remove(key, value)) {
                return true
            }
            sp.edit().putInt(key.key, value!!.toInt()).apply()
            return true
        }
    }

    internal class Int : SpOperator<kotlin.Int>() {
        override fun get(key: CacheKey, default: kotlin.Int?): kotlin.Int? {
            return sp.getInt(key.key, default ?: 0)
        }

        override fun put(key: CacheKey, value: kotlin.Int?): kotlin.Boolean {
            if (remove(key, value)) {
                return true
            }
            sp.edit().putInt(key.key, value!!).apply()
            return true
        }
    }

    internal class Long : SpOperator<kotlin.Long>() {
        override fun get(key: CacheKey, default: kotlin.Long?): kotlin.Long? {
            return sp.getLong(key.key, default ?: 0L)
        }

        override fun put(key: CacheKey, value: kotlin.Long?): kotlin.Boolean {
            if (remove(key, value)) {
                return true
            }
            sp.edit().putLong(key.key, value!!).apply()
            return true
        }
    }

    internal class Float : SpOperator<kotlin.Float>() {
        override fun get(key: CacheKey, default: kotlin.Float?): kotlin.Float? {
            return sp.getFloat(key.key, default ?: 0f)
        }

        override fun put(key: CacheKey, value: kotlin.Float?): kotlin.Boolean {
            if (remove(key, value)) {
                return true
            }
            sp.edit().putFloat(key.key, value!!).apply()
            return true
        }
    }

    internal class String : SpOperator<kotlin.String>() {
        override fun get(key: CacheKey, default: kotlin.String?): kotlin.String? {
            return sp.getString(key.key, default)
        }

        override fun put(key: CacheKey, value: kotlin.String?): kotlin.Boolean {
            if (remove(key, value)) {
                return true
            }
            sp.edit().putString(key.key, value!!).apply()
            return true
        }
    }

    internal class Boolean : SpOperator<kotlin.Boolean>() {
        override fun get(key: CacheKey, default: kotlin.Boolean?): kotlin.Boolean? {
            return sp.getBoolean(key.key, default ?: false)
        }

        override fun put(key: CacheKey, value: kotlin.Boolean?): kotlin.Boolean {
            if (remove(key, value)) {
                return true
            }
            sp.edit().putBoolean(key.key, value!!).apply()
            return true
        }
    }
}