package com.che300.objcache.cache


/** 磁盘缓存不检测文件标志位 */
private const val UNCHECK_FILE_FLAG = 1 shl 1 // 00000010

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
    DISK(1 shl 1),  // 00000100

    /**
     * 磁盘缓存, 但是不检测文件. 比如SP缓存
     */
    DISK_UNCHECK_FILE(DISK.value or UNCHECK_FILE_FLAG),// 00000110

    /**
     * 包含内存缓存和磁盘缓存
     */
    ALL(-1 shr 1 and UNCHECK_FILE_FLAG.inv()),   // -1 >> 1, all bits are 1. 除了第2位以外都是1.// 11111101

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
