package com.che300.objcache.util

import android.content.Context
import okio.buffer
import okio.sink
import okio.source
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile


internal fun Closeable?.safeClose() {
    this ?: return
    try {
        this.close()
    } catch (e: IOException) {
    }
}

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

    internal fun readUtf8(cacheFile: File): String? {
        if (!cacheFile.exists()) {
            return null
        }
        val buffer = cacheFile.source().buffer()
        val readUtf8 = buffer.readUtf8()
        buffer.close()
        return readUtf8
    }

    internal fun readBytes(cacheFile: File): ByteArray? {
        if (!cacheFile.exists()) {
            return null
        }
        val buffer = cacheFile.source().buffer()
        val byteArray = buffer.readByteArray()
        buffer.close()
        return byteArray
    }

    internal fun writeUtf8(cacheFile: File, utf8: String) {
        cacheFile.sink()
            .buffer()
            .writeUtf8(utf8)
            .close()
    }

    internal fun writeBytes(cacheFile: File, byteArray: ByteArray) {
        cacheFile.sink()
            .buffer()
            .write(byteArray)
            .close()
    }

    @Throws(IOException::class)
    internal fun setLastModifiedNow(cacheFile: File) {
        if (!cacheFile.exists()) {
            return
        }
        val now = System.currentTimeMillis()
        val modified = cacheFile.setLastModified(now)
        if (modified) {
            return
        }
        modify(cacheFile)
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
        val accessFile = RandomAccessFile(file, "rwd")
        accessFile.seek(size - 1)
        val lastByte = accessFile.readByte()
        accessFile.seek(size - 1)
        accessFile.write(lastByte.toInt())
        accessFile.close()
    }
}