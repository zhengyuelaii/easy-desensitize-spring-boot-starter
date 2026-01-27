# easy-desensitize-spring-boot-starter

![Build Status](https://github.com/zhengyuelaii/easy-desensitize-spring-boot-starter/actions/workflows/main.yml/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/io.github.zhengyuelaii/easy-desensitize-spring-boot-starter.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

🍃 `easy-desensitize-spring-boot-starter` 是一个基于
[Spring Boot](https://github.com/spring-projects/spring-boot)的 响应数据脱敏组件，
用于在不侵入业务代码的前提下，对接口返回数据进行安全脱敏处理

底层基于
👉 [Easy Desensitize](https://github.com/zhengyuelaii/easy-desensitize-core)

**典型适用场景：**
* 用户信息、手机号、身份证等敏感字段返回
* 后台管理系统 / B 端接口
* 统一响应结构下的数据脱敏
* 支持泛型、集合、嵌套对象

## 特性
* 注解驱动：通过 @MaskingField 注解轻松定义脱敏规则
* 多种脱敏策略：内置常用的脱敏处理器，同时可轻松拓展
* 灵活配置：支持运行时动态配置脱敏规则
* Spring Boot 集成：提供自动配置，零配置快速集成
* 高性能：内置缓存机制，避免重复分析
* 类型安全：支持泛型解析，类型安全的脱敏处理

## 快速开始

> 完整项目示例：[easy-desensitize-samples](https://github.com/zhengyuelaii/easy-desensitize-samples)

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
### 3. 在Controller启用脱敏

在 **方法或类** 上添加 @ResponseMasking 注解。

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

响应结果

```json
{
    "username": "李*龙",
    "password": "******",
    "address": "上海"
}
```
## 配置项

`easy-desensitize-spring-boot-starter` 提供了少量全局配置项，用于控制脱敏行为。
**所有配置均为可选，默认即可满足大多数场景。**

**配置项说明**

| 配置项                                  | 类型      | 默认值  | 说明        |
|--------------------------------------|---------|------|-----------|
| easy-desensitize.enabled             | boolean | true | 是否启用脱敏功能  |
| easy-desensitize.use-global-cache    | boolean | true | 是否启用全局缓存  |
| easy.desensitize.use-global-resolver | boolean | true | 是否启用全局解析器 |

**示例**

```yaml
easy:
  desensitize:
    enabled: true
    use-global-cache: true
    use-global-resolver: true
```

## 高级用法

### 1. 自定义脱敏处理器
> 推荐结合`Hutool`的 [DesensitizedUtil](https://doc.hutool.cn/pages/DesensitizedUtil)使用

当内置处理器无法满足需求时，可自定义脱敏规则。

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

使用方式与内置处理器一致：

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

### 2. 动态指定脱敏规则

通过 `@ResponseMasking` 注解可以动态指定脱敏规则，用于在特定接口中临时添加或排除某些字段的脱敏处理。

1. 排除特定字段

即使实体类标注了注解，也可以在特定接口排除脱敏。

```java
@ResponseMasking(excludeFields = {"name"})
@GetMapping("/list")
public List<Person> list() { ... }
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

响应结果

```json
{
    "name": "张*凡",
    "mobile": "137****4586",
    "idNumber": "13**************95"
}
```

3. 类级统一脱敏

为Controller添加`@ResponseMasking`注解，则该类下所有接口默认开启脱敏。

```java
@RestController
@RequestMapping("/map")
@ResponseMasking(fields = {
        @MaskingField(name = "name", typeHandler = NameMaskingHandler.class),
        @MaskingField(name = "mobile", typeHandler = MobileMaskingHandler.class)
})
public class MapDataController {
    ...
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
        ...
    }

}
```

### 2. 脱敏拦截器

通过拦截器动态控制是否执行脱敏逻辑。

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

适用于统一响应结构（如 Result\<T\>、Page\<T\>），
用于 快速定位真正需要脱敏的数据对象，减少反射路径。

示例：Result

```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    // getter/setter
}

// 继承AbstractMaskingDataResolver实现resolveInternal方法
public class ResultMaskingDataResolver extends AbstractMaskingDataResolver<Result<?>> {

    @Override
    protected Object resolveInternal(Result<?> source) {
        return source.getData();
    }

}
```
配置解析器（支持同时配置多个解析器）

```java

@Configuration
public class EasyDesensitizeConfig {
    
    @Bean
    public ResultMaskingDataResolver resultMaskingDataResolver() {
        return new ResultMaskingDataResolver();
    }
    
    @Bean
    public MaskingDataResolver<Page<?>> pageMaskingDataResolver() {
        return new AbstractMaskingDataResolver<Page<?>>() {
            @Override
            protected Object resolveInternal(Page<?> source) {
                return source.getRecords().iterator();
            }
        };
    }
    
}
```

> 完整示例：[easy-desensitize-sample-common-result](https://github.com/zhengyuelaii/easy-desensitize-samples/tree/main/easy-desensitize-sample-common-result)

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request！

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

------

## 📄 开源协议

本项目基于 [Apache License 2.0](https://www.google.com/search?q=LICENSE) 协议开源。