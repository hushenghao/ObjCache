package com.che300.objcache.util

import android.content.Context
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile


/**
 * 文件相关工具
 *
 * @author hsh
 * @since 2020/7/20 6:02 PM
 */
object Files {

    /**
     * 默认缓存路径
     */
    internal fun defaultCacheDir(context: Context): File {
        val file = File(context.externalCacheDir ?: context.cacheDir, "ObjCache")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    internal fun readUtf8(file: File): String? {
        if (!file.exists()) {
            return null
        }
        return file.source()
            .buffer()
            .use { it.readUtf8() }
    }

    internal fun readBytes(file: File): ByteArray? {
        if (!file.exists()) {
            return null
        }
        return file.source()
            .buffer()
            .use { it.readByteArray() }
    }

    internal fun writeUtf8(file: File, utf8: String) {
        if (!file.exists()) {
            file.createNewFile()
        }
        file.sink()
            .buffer()
            .writeUtf8(utf8)
            .close()
    }

    internal fun writeBytes(file: File, byteArray: ByteArray) {
        if (!file.exists()) {
            file.createNewFile()
        }
        file.sink()
            .buffer()
            .write(byteArray)
            .close()
    }

    @Throws(IOException::class)
    internal fun setLastModifiedNow(file: File) {
        if (!file.exists()) {
            return
        }
        val now = System.currentTimeMillis()
        val modified = file.setLastModified(now)
        if (modified) {
            return
        }
        modify(file)
    }

    @Throws(IOException::class)
    private fun modify(file: File) {
        val size = file.length()
        if (size == 0L) {
            if (!file.delete() || !file.createNewFile()) {
                throw IOException("Error recreate zero-size file $file")
            }
            return
        }
        RandomAccessFile(file, "rwd")
            .use {
                it.seek(size - 1)
                val lastByte = it.readByte()
                it.seek(size - 1)
                it.write(lastByte.toInt())
            }
    }
}