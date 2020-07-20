package com.che300.objcachedemo

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.che300.objcache.*
import com.che300.objcache.cache.CacheStrategy
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    class SerializableTest(var string: String) : Serializable {
        override fun toString(): String {
            return string
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ObjCache.debug(true)

//        ObjCache.Builder(this)
//            .debug(true)
//            .maxDiskCount(8)
//            .maxDiskSize(1024 * 1)
//            .create()

        Thread {
            for (i in (0..10)) {
                ObjCache.putSerializable("Serializable$i", SerializableTest("测试Serializable$i"))
                Thread.sleep(1000)
            }
        }.start()

        val serializable = ObjCache
            .getSerializable("Serializable", SerializableTest("默认值"))
        Log.i(TAG, "onCreate: " + serializable)

        ObjCache.putSerializable("Serializable", SerializableTest("新的值"))

        val serializable1 = ObjCache
            .getSerializable("Serializable", SerializableTest("默认值2"))
        Log.i(TAG, "onCreate: " + serializable1)

        Log.i(TAG, "=======================")

        val rect = ObjCache.with(Rect.CREATOR)
            .get("rect", Rect())
        Log.i(TAG, "读取: " + rect)

        ObjCache.with(Rect.CREATOR)
            .put("rect", Rect(1, 2, 3, 4))

        val rect2 = ObjCache.with(Rect.CREATOR)
            .cacheStrategy(CacheStrategy.DISK)
            .get("rect", Rect(4, 3, 2, 1))
        Log.i(TAG, "再次读取: " + rect2)


        val boolean = ObjCache.getBoolean("first_in", true)
        Log.i(TAG, "onCreate: " + boolean)
        if (boolean) {
            ObjCache.putBoolean("first_in", false)
        }
    }
}