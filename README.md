##### 前提：

编译前请检查 *build.gradle* 中的 *versionCode* 和 *SDK_DEBUG* 与 *cloud/bjx/mm/sdk/internal/Config.java* 中的配置是否一致
如若不一致，编译时会抛出异常



##### 编译：

```shell
# 最终 aar 会输出到 output 文件夹中： mmrtc_release_0.3.1.aar 
➜ ./make.py --make
```



##### 复制 *aar* 文件到 *demo* libs 中：

```shell
➜ ./make.py --install
```



##### 清空编译：

```shell
➜ ./make.py --clean
```

