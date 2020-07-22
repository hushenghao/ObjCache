package com.che300.objcache.cache

/**
 * 缓存策略
 */
enum class CacheStrategy(val value: Int) {

    /**
     * 不使用缓存,直接使用默认值
     */
    NONE(0),

    /**
     * 内存缓存,优先级高于磁盘缓存
     */
    MEMORY(1 shl 1),

    /**
     * 磁盘缓存
     */
    DISK(1 shl 3),

    /**
     * 包含内存缓存和磁盘缓存
     */
    ALL(-1 shr 1),   // -1 >> 1 all bits are 1

}

internal infix fun CacheStrategy.and(other: CacheStrategy): Int {
    return this.value and other.value
}

/**
 * 是否有当前缓存策略
 *
 * @param strategy 需要判断的入参
 * @param flag 缓存类型
 * @return true 有、false 没有
 */
internal fun hasStrategy(strategy: Int, flag: CacheStrategy): Boolean {
    return strategy and flag.value > 0
}
