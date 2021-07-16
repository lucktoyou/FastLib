### 功能预览
<img src="/preview/main.jpg" width="380px"/>

### 使用方式
在根目录下的build.gradle里添加maven仓库
```groovy
allprojects {
    repositories {
	    //...
	    maven { url 'https://jitpack.io' }
	}
}
```
添加依赖
```groovy
dependencies {
    //...
    implementation 'com.github.lucktoyou:FastLib:release'
}
```

### 项目包说明
+ annotation    整个项目中的自定义注解
+ base          基础模块 如封装的Activity，封装Dialog，rx风格任务链，adapter封装类，RecyclerView条目装饰等
+ bean          事件和其他实体类 一般用户除了事件类，不使用此包中其他类
+ db            数据库封装模块 此封装的数据库是非orm的
+ net           网络封装模块
+ utils         非常通用的工具类包
+ widget        基本上都是继承View或者相关的功能模块

### 第三方依赖
+ Gson 
+ androidx兼容包
+ google material
+ LocalBroadcastManager
+ com.orhanobut:logger
