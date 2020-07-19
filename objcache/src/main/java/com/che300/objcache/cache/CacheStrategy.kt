package com.che300.objcache.cache

interface CacheStrategy<T> {

    companion object {
        const val NONE = 0
        const val MEMORY = 1 shl 1
        const val DISK = 1 shl 3

        const val ALL = -1 shr 1   // -1 >> 1 all bits are 1

        fun hasStrategy(strategy: Int, strategyFlag: Int): Boolean {
            return strategy and strategyFlag > 0
        }
    }

    fun cacheStrategy(strategy: Int): T
}
