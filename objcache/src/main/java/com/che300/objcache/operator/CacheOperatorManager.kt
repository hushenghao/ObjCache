package com.che300.objcache.operator

import android.os.Parcelable
import java.io.Serializable
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * 缓存操作者管理器
 */
class CacheOperatorManager {

    private val cacheOperator = HashMap<Type, CacheOperator<*>>()

    fun <T> register(type: Type, operator: CacheOperator<T>) {
        cacheOperator[type] = operator
    }

    fun <T> get(type: Type): CacheOperator<T> {
        var operator = cacheOperator[type] as? CacheOperator<T>
        if (operator != null) {
            return operator
        }

        var typeOfT = type
        when (type) {
            is Class<*> -> {
                val clazz = typeOfT as Class<*>
                val genericInterfaces = clazz.genericInterfaces
                for (`interface` in genericInterfaces) {
                    val iClass = `interface` as? Class<*> ?: continue
                    if (iClass == Parcelable::class.java) {
                        typeOfT = Parcelable::class.java
                        break
                    } else if (iClass == Serializable::class.java) {
                        typeOfT = Serializable::class.java
                        break
                    }
                }
            }
            is ParameterizedType -> {
                return GsonOperator<T>(type)
            }
            is GenericArrayType -> {
                return GsonOperator<T>(type)
            }
            is WildcardType -> {

            }
        }
        operator = cacheOperator[typeOfT] as? CacheOperator<T>
        return operator ?: GsonOperator<T>(typeOfT)
    }

    private fun getOperator(type: Type): CacheOperator<*> {
        when (type) {
            is Class<*> -> {
                val genericInterfaces = type.genericInterfaces
                for (`interface` in genericInterfaces) {
                    val iClass = `interface` as? Class<*> ?: continue
                    if (iClass == Parcelable::class.java) {
                        return BundleOperator()
                    } else if (iClass == Serializable::class.java) {
                        return BundleOperator()
                    }
                }
            }
            is ParameterizedType -> {
                val actualTypeArguments = type.actualTypeArguments
                val size = actualTypeArguments.size
                if (size >= 1) {
                    val key = getOperator(actualTypeArguments[0])
                    if (size == 1) {
                        return key
                    }
                    val value = getOperator(actualTypeArguments[1])
                    if (key.javaClass == value.javaClass) {
                        return key
                    }
                }
            }
            is GenericArrayType -> {
                return getOperator(type.genericComponentType)
            }
        }
        return GsonOperator<Any>(type)
    }
}