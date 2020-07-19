package com.che300.objcachedemo

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.che300.objcache.ObjCache
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

        ObjCache.Builder(this)
            .debug(true)
            .create()

        val serializable = ObjCache.get()
            .getSerializable("Serializable", SerializableTest("默认值"))
        Log.i(TAG, "onCreate: " + serializable)

        ObjCache.edit()
            .putSerializable("Serializable", SerializableTest("新的值"))

        val serializable1 = ObjCache.get()
            .getSerializable("Serializable", SerializableTest("默认值2"))
        Log.i(TAG, "onCreate: " + serializable1)

        Log.i(TAG, "=======================")

        val rect = ObjCache.get()
            .cacheStrategy(CacheStrategy.DISK)
            .getParcelable("rect", Rect.CREATOR, Rect())
        Log.i(TAG, "读取: " + rect)

        ObjCache.edit()
            .cacheStrategy(CacheStrategy.DISK)
            .putParcelable("rect", Rect(1, 2, 3, 4))

        val rect2 = ObjCache.get()
            .cacheStrategy(CacheStrategy.DISK)
            .getParcelable("rect", Rect.CREATOR, Rect())
        Log.i(TAG, "再次读取: " + rect2)


        val boolean = ObjCache.get()
            .getBoolean("first_in", true)
        Log.i(TAG, "onCreate: " + boolean)
        if (boolean) {
            ObjCache.edit().putBoolean("first_in", false)
        }
    }
}