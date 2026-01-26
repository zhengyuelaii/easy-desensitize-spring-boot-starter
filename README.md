# easy-desensitize-spring-boot-starter

## 介绍

🍃`easy-desensitize-spring-boot-starter` 将帮助您基于[Spring Boot](https://github.com/spring-projects/spring-boot)使用[Easy Desensitize](https://github.com/zhengyuelaii/easy-desensitize-core)

## 特性
* 注解驱动：通过 @MaskingField 注解轻松定义脱敏规则
* 多种脱敏策略：内置常用的脱敏处理器，同时可轻松拓展
* 灵活配置：支持运行时动态配置脱敏规则
* Spring Boot 集成：提供自动配置，零配置快速集成
* 高性能：内置缓存机制，避免重复分析
* 类型安全：支持泛型解析，类型安全的脱敏处理

## 快速开始

> 完整代码示例见：[easy-desensitize-samples](https://github.com/zhengyuelaii/easy-desensitize-samples)

### 1. 添加依赖
```xml
<dependency>
    <groupId>io.github.zhengyuelaii</groupId>
    <artifactId>easy-desensitize-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```
### 2. 创建实体类

在字段上使用 @MaskingField 定义脱敏策略。

```java
import io.github.zhengyuelaii.desensitize.core.annotation.MaskingField;
import io.github.zhengyuelaii.desensitize.core.handler.FixedMaskHandler;
import io.github.zhengyuelaii.desensitize.core.handler.KeepFirstAndLastHandler;

public class User {
    @MaskingField(typeHandler = KeepFirstAndLastHandler.class)
    private String name; // 李小龙 -> 李*龙
    @MaskingField(typeHandler = FixedMaskHandler.class)
    private String password; // 123456 -> ******
    private String address;

    // getter/setter
}
```
### 3. 在Controller中使用

在 Controller 方法或类上添加 @ResponseMasking 注解。

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/get")
    @ResponseMasking
    public User get() {
        return new User( "李小龙", "12345678", "上海");
    }

}
```

输出

```json
{
    "username": "李*龙",
    "password": "******",
    "address": "上海"
}
```

## 高级用法

### 1. 自定义脱敏处理器
> 推荐搭配`Hutool`的 [DesensitizedUtil](https://doc.hutool.cn/pages/DesensitizedUtil)使用

如果默认处理器无法满足需求，可自定义脱敏规则，自定义脱敏处理器需要实现`io.github.zhengyuelaii.desensitize.core.handler.MaskingHandler`接口

1. 创建一个手机号脱敏处理器

```java
import cn.hutool.core.util.DesensitizedUtil;

/**
 * 手机号脱敏处理器
 */
public class MobileMaskingHandler implements MaskingHandler {
    @Override
    public String getMaskingValue(String value) {
        // 搭配hutools 的 DesensitizedUtil 使用
        return DesensitizedUtil.mobilePhone(value);
    }
}
```

2. 创建实体类

```java
public class Person {
    @MaskingField(typeHandler = NameMaskingHandler.class)
    private String name;
    @MaskingField(typeHandler = MobileMaskingHandler.class)
    private String mobile;
    @MaskingField(typeHandler = IdNumberMaskingHandler.class)
    private String idNumber;
    // getter/setter
}
```
3. 在控制器中使用

```java
@RestController
@RequestMapping("/person")
public class PersonResController {
    
    @GetMapping("/list")
    @ResponseMasking
    public List<Person> list() {
        Person person = new Person();
        person.setName("张小凡");
        person.setMobile("13700004586");
        person.setIdNumber("130535202206145195");
        return Collections.singletonList(person);
    }

}
```

* 输出

```json
[
    {
        "name": "张*凡",
        "mobile": "137****4586",
        "idNumber": "13**************95"
    }
]
```

### 2. 动态指定脱敏规则

通过 `@ResponseMasking` 注解可以动态指定脱敏规则，用于在特定接口中临时添加或排除某些字段的脱敏处理。

1. 排除特定字段

即使实体类标注了注解，也可以在特定接口排除脱敏。

```java
@RestController
@RequestMapping("/person")
public class PersonResController {

    @GetMapping("/list")
    @ResponseMasking(excludeFields = { "name" })
    public List<Person> list() {
        Person person = new Person();
        person.setName("张小凡");
        person.setMobile("13700004586");
        person.setIdNumber("130535202206145195");

        return Collections.singletonList(person);
    }

}
```

输出
```json
[
    {
        "name": "张小凡",
        "mobile": "137****4586",
        "idNumber": "13**************95"
    }
]
```

2. 动态指定字段

适用于未标记注解或无法修改源码的类以及 Map<String, Object> 数据。

```java
@RestController
@RequestMapping("/person")
public class PersonResController {
    
    @GetMapping("/map")
    @ResponseMasking(fields = {
            @MaskingField(name = "name", typeHandler = KeepFirstAndLastHandler.class),
            @MaskingField(name = "mobile", typeHandler = MobileMaskingHandler.class),
            @MaskingField(name = "idNumber", typeHandler = IdNumberMaskingHandler.class)
    })
    public Map<String, Object> map() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "张小凡");
        data.put("mobile", "13700004586");
        data.put("idNumber", "130535202206145195");

        return data;
    }

}
```

输出

```json
{
    "name": "张*凡",
    "mobile": "137****4586",
    "idNumber": "13**************95"
}
```

3. 类级全局配置

为Controller添加`@ResponseMasking`注解，则该类下所有接口默认开启脱敏。

```java
@RestController
@RequestMapping("/map")
@ResponseMasking(fields = {
        @MaskingField(name = "name", typeHandler = NameMaskingHandler.class),
        @MaskingField(name = "mobile", typeHandler = MobileMaskingHandler.class)
})
public class MapDataController {

    @RequestMapping("/get")
    public Map<String, Object> list() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "张小凡");
        data.put("mobile", "13888888888");
        return data;
    }

}
```

输出
```json
{
  "name": "张小凡",
  "mobile": "138****8888"
}
```

> ⚠️ 注意：当类与方法同时添加`@ResponseMasking`注解时，**方法级注解优先级更高。**

4. 忽略脱敏

如果Controller上开启了脱敏，个别接口可以使用 @IgnoreResponseMasking 强制关闭。

```java
@RequestMapping("/map")
@ResponseMasking(fields = {
        @MaskingField(name = "name", typeHandler = NameMaskingHandler.class),
        @MaskingField(name = "mobile", typeHandler = MobileMaskingHandler.class)
})
public class MapDataController {
    
    @GetMapping("/ignore")
    @IgnoreResponseMasking
    public Map<String,  Object> ignore() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "张小凡");
        data.put("mobile", "13888888888");
        return data;
    }

}
```

### 2. 脱敏拦截器

当存在某些权限需返回不脱敏的数据时，可添加脱敏拦截器控制是否脱敏

```java
// 创建拦截器
public class MyDesensitizeInterceptor implements EasyDesensitizeInterceptor {

    @Override
    public boolean preHandle(Object body, MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
        String userId = request.getHeaders().getFirst("x-user-id");
        return !Objects.equals("1", userId); // 用户ID=1时跳过脱敏
    }

}

// 配置拦截器
@Configuration
public class MyConfig {
    @Bean
    public MyDesensitizeInterceptor myDesensitizeInterceptor() {
        return new MyDesensitizeInterceptor();
    }
}
```

### 3. 全局解析器

```java
// TODO
```

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request！

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

------

## 📄 开源协议

本项目基于 [Apache License 2.0](https://www.google.com/search?q=LICENSE) 协议开源。