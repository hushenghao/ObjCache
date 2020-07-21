package com.che300.objcache.operator

import android.os.Parcelable
import java.io.Serializable
import java.lang.reflect.Type

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
        if (type is Class<*>) {
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
//        when (type) {
//            is ParameterizedType -> {
//            }
//            is GenericArrayType -> {
//            }
//            is WildcardType -> {
//            }
//            is Class<*> -> {
//            }
//        }
        operator = cacheOperator[typeOfT] as? CacheOperator<T>
        return operator ?: GsonOperator<T>(typeOfT)
    }
}