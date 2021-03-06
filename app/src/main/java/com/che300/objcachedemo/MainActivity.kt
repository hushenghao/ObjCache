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

//        ObjCache.clear()

        ObjCache.Builder(this)
            .debug(true)
            .maxDiskCount(100)
            .create()

        Thread {
            for (i in (0..10)) {
                ObjCache.with(SerializableTest::class.java)
                    .cacheStrategy(CacheStrategy.DISK)
                    .put("Serializable$i", SerializableTest("测试Serializable缓存$i"))
                Thread.sleep(1000)
            }
        }
            .start()

        val serializable = ObjCache
            .getSerializable("serializable_test", SerializableTest("默认值"))
        Log.i(TAG, "onCreate: " + serializable)

        ObjCache.putSerializable("serializable_test", SerializableTest("新的值"))

        val serializable1 = ObjCache
            .getSerializable("serializable_test", SerializableTest("默认值2"))
        Log.i(TAG, "onCreate: " + serializable1)

        Log.i(TAG, "=======================")

        val rect = ObjCache.with<Rect>()
            .cacheStrategy(CacheStrategy.DISK)
            .get("rect", Rect.CREATOR, Rect())
        Log.i(TAG, "读取: " + rect)

        ObjCache.with<Rect>()
            .cacheStrategy(CacheStrategy.DISK)
            .put("rect", Rect(1, 2, 3, 4))

        val rect2 = ObjCache.with<Rect>()
            .cacheStrategy(CacheStrategy.DISK)
            .get("rect", Rect.CREATOR, Rect(-1, -1, -1, -1))
        Log.i(TAG, "再次读取: " + rect2)


        val boolean = ObjCache.getBoolean("first_in", true)
        Log.i(TAG, "onCreate: " + boolean)
        if (boolean) {
            ObjCache.putBoolean("first_in", false)
        }


        val list = (1..1000).asSequence()
            .map { "字符串测试$it" }
            .toMutableList()

        val bundle = Bundle()
        bundle.putStringArrayList("list", list as ArrayList<String>)
        ObjCache.with<Bundle>()
            .cacheStrategy(CacheStrategy.DISK)
            .put("bundle_list", bundle)
        val getBundle = ObjCache.with<Bundle>()
            .cacheStrategy(CacheStrategy.DISK)
            .get("bundle_list", Bundle())
        val bundleList = getBundle?.getStringArrayList("list")
        Log.i(TAG, "onCreate: list size: " + bundleList?.size)

        ObjCache.with<Serializable>()
            .cacheStrategy(CacheStrategy.DISK)
            .put("list_test", list)
        val list2 = ObjCache.with<Serializable>()
            .cacheStrategy(CacheStrategy.DISK)
            .get("list_test", null) as? ArrayList<String>
        Log.i(TAG, "onCreate: list size: " + list2?.size)


        val rects = (1..10).asSequence()
            .map { Rect(it, it, it, it) }
            .toMutableList()
        ObjCache.with<List<Rect>>()
            .put("rect_list", rects)
        val rectList = ObjCache.with<List<Rect>>()
            .get("rect_list", emptyList())
        Log.i(TAG, "onCreate: rect list size: $rectList")

        ObjCache.with<List<Rect>>()
            .remove("rect_list")
    }
}