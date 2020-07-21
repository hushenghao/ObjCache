package com.che300.objcache.cache

import com.che300.objcache.util.log
import com.che300.objcache.util.logw
import java.io.File
import java.util.concurrent.Callable

/**
 * 磁盘缓存删除管理
 */
internal class LruDiskTrim(private val strategy: Strategy) {


    /**
     * 默认策略、不限制
     */
    class LazyStrategy : Strategy {
        override fun accept(file: File, totalCount: Int, totalSize: Long): Boolean {
            return true
        }
    }

    /**
     * 限制缓存总大小
     */
    class SizeStrategy(private val maxSize: Long) : Strategy {
        override fun accept(file: File, totalCount: Int, totalSize: Long): Boolean {
            return maxSize >= totalSize
        }
    }

    /**
     * 限制缓存数量
     */
    class CountStrategy(private val maxCount: Int) : Strategy {
        override fun accept(file: File, totalCount: Int, totalSize: Long): Boolean {
            return maxCount >= totalCount
        }
    }

    /**
     * 磁盘缓存删除策略
     */
    interface Strategy {
        fun accept(file: File, totalCount: Int, totalSize: Long): Boolean
    }

    /**
     * 删除过期磁盘缓存
     */
    private fun trim(files: List<File>) {
        var totalSize = files.asSequence()
            .map { it.length() }
            .reduce { acc, l -> acc + l }
        var totalCount: Int = files.size
        var trimCount = 0
        for (file in files) {
            synchronized(LruDiskTrim::class.java) {
                if (!file.exists()) {
                    totalSize--
                    return@synchronized
                }
                val accepted = strategy.accept(file, totalCount, totalSize)
                if (!accepted) {
                    val length = file.length()
                    val deleted = file.delete()
                    if (deleted) {
                        totalSize -= length
                        totalCount--
                        log("DEL disk cache: $file")
                        trimCount++
                    } else {
                        logw("DEL disk cache error: $file")
                    }
                } else {
                    if (trimCount > 0) {
                        log("DEL disk cache size: $trimCount")
                    }
                    return
                }
            }
        }
    }

    internal inner class TrimCallable(private val cacheDir: File) : Callable<Unit> {

        override fun call() {
            val fileList = cacheDir.listFiles()?.toList() ?: return
            trim(fileList.sortedBy { it.lastModified() })
        }
    }
}