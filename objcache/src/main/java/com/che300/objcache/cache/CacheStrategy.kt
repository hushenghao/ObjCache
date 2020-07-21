package com.che300.objcache.cache

/**
 * 缓存策略
 */
interface CacheStrategy {

    companion object {

        /**
         * 不使用缓存,直接使用默认值
         */
        const val NONE = 0

        /**
         * 内存缓存,优先级高于磁盘缓存
         */
        const val MEMORY = 1 shl 1

        /**
         * 磁盘缓存
         */
        const val DISK = 1 shl 3

        /**
         * 包含内存缓存和磁盘缓存
         */
        const val ALL = -1 shr 1   // -1 >> 1 all bits are 1

        /**
         * 是否有当前缓存策略
         *
         * @param strategy 需要判断的入参
         * @param strategyFlag 缓存类型
         * @return true 有、false 没有
         */
        fun hasStrategy(
            strategy: Int,
            @com.che300.objcache.annotation.CacheStrategy strategyFlag: Int
        ): Boolean {
            return strategy and strategyFlag > 0
        }
    }

}
