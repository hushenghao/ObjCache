@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmMultifileClass
@file:JvmName("ObjCacheKt")

package com.che300.objcache

import android.os.Parcelable
import com.che300.objcache.operator.CacheOperator
import com.che300.objcache.operator.ParcelableOperator
import com.che300.objcache.request.RequestBuilder
import java.io.Serializable


/**
 * get
 */
inline fun ObjCache.Companion.getInt(key: String, default: Int = 0): Int {
    return with(Int::class.java).get(key, default) ?: 0
}

inline fun ObjCache.Companion.getLong(key: String, default: Long = 0L): Long {
    return this.with(Long::class.java).get(key, default) ?: 0L
}

inline fun ObjCache.Companion.getFloat(key: String, default: Float = 0f): Float {
    return this.with(Float::class.java).get(key, default) ?: 0f
}

inline fun ObjCache.Companion.getBoolean(key: String, default: Boolean = false): Boolean {
    return this.with(Boolean::class.java).get(key, default) ?: false
}

inline fun ObjCache.Companion.getString(key: String, default: String? = null): String? {
    return this.with(String::class.java).get(key, default)
}

inline fun <reified T : Parcelable> ObjCache.Companion.getParcelable(
    key: String,
    creator: Parcelable.Creator<T>,
    default: T?
): T? {
    return this.with(creator).get(key, default)
}

inline fun <T : Serializable> ObjCache.Companion.getSerializable(
    key: String,
    default: T?
): T? {
    return this.with(Serializable::class.java).get(key, default) as? T
}

inline fun <reified T : Parcelable> RequestBuilder<T>.get(
    key: String,
    creator: Parcelable.Creator<T>,
    default: T?
): T? {
    this.operator = parcelableOperator<T>(creator)
    return this.get(key, default)
}


/**
 * other
 */
inline fun <reified T : Parcelable> Any.parcelableOperator(creator: Parcelable.Creator<T>): CacheOperator<T> {
    return ParcelableOperator.Get(creator)
}

inline fun <reified T : Parcelable> ObjCache.Companion.with(creator: Parcelable.Creator<T>): RequestBuilder<T> {
    return this.with(parcelableOperator(creator))
}

inline fun <reified T> ObjCache.Companion.with(): RequestBuilder<T> {
    return this.with(T::class.java)
}


/**
 * put
 */
inline fun ObjCache.Companion.putInt(key: String, value: Int): Boolean {
    return this.with(Int::class.java).put(key, value)
}

inline fun ObjCache.Companion.putLong(key: String, value: Long): Boolean {
    return this.with(Long::class.java).put(key, value)
}

inline fun ObjCache.Companion.putFloat(key: String, value: Float): Boolean {
    return this.with(Float::class.java).put(key, value)
}

inline fun ObjCache.Companion.putBoolean(key: String, value: Boolean): Boolean {
    return this.with(Boolean::class.java).put(key, value)
}

inline fun ObjCache.Companion.putString(key: String, value: String?): Boolean {
    return this.with(String::class.java).put(key, value)
}

inline fun <T : Parcelable> ObjCache.Companion.putParcelable(key: String, value: T?): Boolean {
    return this.with(Parcelable::class.java).put(key, value)
}

inline fun <T : Serializable> ObjCache.Companion.putSerializable(key: String, value: T?): Boolean {
    return this.with(Serializable::class.java).put(key, value)
}