package com.che300.objcache.cache


/**
 * 缓存策略
 */
enum class CacheStrategy(val value: Int) {

    /**
     * 不使用缓存,直接使用默认值
     */
    NONE(0),        // 00000000

    /**
     * 内存缓存,优先级高于磁盘缓存
     */
    MEMORY(1),      // 00000001

    /**
     * 磁盘缓存
     */
    DISK(1 shl 1),  // 00000010

    /**
     * 包含内存缓存和磁盘缓存
     */
    ALL(-1 shr 1),   // -1 >> 1, all bits are 1.

}

/**
 * 位与重载运算
 */
internal infix fun CacheStrategy.and(other: CacheStrategy): Int {
    return this.value and other.value
}

/**
 * 当前策略是否有传入的缓存策略
 *
 * @param flag 缓存类型
 * @return true 有、false 没有
 */
internal fun Int.hasStrategy(flag: CacheStrategy): Boolean {
    return this and flag.value > 0
}
