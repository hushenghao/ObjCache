package com.che300.objcache.operator

import android.os.Parcelable
import java.io.Serializable
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CacheOperatorManager {

    private val cacheOperator = HashMap<Type, CacheOperator<*>>()

    fun <T> register(type: Type, operator: CacheOperator<T>) {
        cacheOperator[type] = operator
    }

    fun <T> get(type: Type): CacheOperator<T> {
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
        }
        val cacheOperator = cacheOperator[typeOfT] as? CacheOperator<T>
        return cacheOperator ?: GsonOperator<T>(typeOfT)
    }
}