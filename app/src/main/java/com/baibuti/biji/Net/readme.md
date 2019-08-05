# `Net/` 

### Structure

```tree
.
│  NetUtil.java                 -> 封装好的同步 GET POST
│  Urls.java                    -> URL 统一存放
│                               
├─Models                        -> 请求头 返回头 返回体 一般模型
│  │  RespType.java             -> NetUtil 统一返回的类型
│  │                            
│  ├─ReqBody                    -> 请求体
│  │                            
│  ├─RespBody                   -> 返回体
│  │      MessageResp.java      -> 对应于后端的 Message (存放错误信息等)
│  │                            
│  └─RespObj                    -> 一般模型
│                               
└─Modules                       -> 分模块处理
   └...                 
```

### RespType

+ `body: String`: `response.body().string()`
+ `headers: Headers`: `response.headers()`
+ `code: int`: `response.code()`

> 关于 `body` 的 `IllegalStateException` 暂时直接存储 `String`