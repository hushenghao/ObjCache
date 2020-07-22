# ObjCache

一个简化Android对象持久化存储操作的工具组件。支持二级缓存，支持自定义序列化方案

支持的类型：

* 基本数据类型（除byte、char、double）、字符类型
* Serializable、Parcelable，同时还支持Bundle进行混合存储。
* 非Serializable、Parcelable类型和List、Map对象，默认会使用Gson进行序列化与反序列化操作，请确保缓存对象支持Gson


## 开始使用

### 项目集成
```groovy
// 内网仓库地址
maven { url 'http://172.16.0.236:8081/repository/maven-public/' }

// 项目依赖
implementation("com.che300.utils:obj-cache:1.0.0-20200722.062500-2")
```

### 简单使用

* Put

```kotlin
ObjCache.putInt("int", 1)
ObjCache.putBoolean("boolean", true)

ObjCache.putSerializable("test", SerializableImpl())
ObjCache.putParcelable("rect", Rect(1, 2, 3, 4))

```

* Get

```kotlin
val int = ObjCache.getInt("int", 1)
val boolean = ObjCache.getBoolean("boolean", true)


val test = ObjCache.getSerializable("test", null)
val rect = ObjCache.getParcelable("rect", Rect.CREATOR, null)
```

### 高级使用

1.自定义初始化

```kotlin
ObjCache.Builder(context)
    .debug(BuildConfig.DEBUG)   // 日志开关
    .maxMemoryCount(1000)       // 最大内存缓存数量
    .maxDiskCount(1000)         // 磁盘缓存清理策略
    .addCacheOperator(Fast::class.java, FastCacheOperator())
    // 自定义序列化方案，使用Type区分。可以通过当前方法覆盖内置方案
    .create()
```

2.使用

* Put
```kotlin
ObjCache.with<Rect>()                   // 定义序列化类型
    .cacheStrategy(CacheStrategy.DISK)  // 缓存策略
    .put("rect", Rect(1, 2, 3, 4))      // 根据策略存入相应缓存
```

* Get
```kotlin
val rect = ObjCache.with<Rect>()        // 定义序列化类型
    .cacheStrategy(CacheStrategy.DISK)  // 缓存策略
    .get("rect", Rect.CREATOR, null)    // 从指定策略获取缓存, 单独获取Parcelable类型缓存时需要传递CREATOR
```

* Remove
```kotlin
ObjCache.with<Rect>()
    .remove("rect")
```

3.清空缓存

```kotlin
ObjCache.clear()
```

4.自定义序列化方案

需要实现CacheOperator接口，实现get、put和remove方法。最后在初始化时进行注册。

内置序列化方案

|Type|实现类|序列化方案|
|:--|:-:|:-:|
|基本数据类型|SpOperator|SharedPreferences|
|String|SpOperator$_String|SharedPreferences|
|Parcelable|ParcelableOperator|Parcelable|
|Bundle|BundleOperator|Parcelable|
|Serializable|SerializableOperator$Fast|Serializable|
|List|GsonOperator|Gson|
|Map|GsonOperator|Gson|
|Object|GsonOperator|Gson|