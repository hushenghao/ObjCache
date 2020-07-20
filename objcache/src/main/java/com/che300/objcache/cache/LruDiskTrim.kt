package com.che300.objcache.cache

import com.che300.objcache.util.log
import com.che300.objcache.util.logw
import java.io.File
import java.util.concurrent.Callable

/**
 * @author hsh
 * @since 2020/7/20 4:35 PM
 */
internal class LruDiskTrim(private val strategy: Strategy) {


    class LazyStrategy : Strategy {
        override fun accept(file: File, totalCount: Int, totalSize: Long): Boolean {
            return true
        }
    }

    class SizeStrategy(private val maxSize: Long) : Strategy {
        override fun accept(file: File, totalCount: Int, totalSize: Long): Boolean {
            return maxSize >= totalSize
        }
    }

    class CountStrategy(private val maxCount: Int) : Strategy {
        override fun accept(file: File, totalCount: Int, totalSize: Long): Boolean {
            return maxCount >= totalCount
        }
    }

    interface Strategy {
        fun accept(file: File, totalCount: Int, totalSize: Long): Boolean
    }

    private fun trim(files: List<File>) {
        var totalSize = files.asSequence()
            .map { it.length() }
            .reduce { acc, l -> acc + l }
        var totalCount: Int = files.size
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
                    } else {
                        logw("DEL disk cache error: $file")
                    }
                } else {
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