#### 简介
MyBatisjj-mysql 是一个优雅的 MyBatis 增强框架，它非常轻量、同时拥有极高的性能与灵活性。MyBatisjj-mysql 能够极大地提高我们的开发效率和开发体验，让我们有更多的时间专注于自己的事情。

#### 安装

```
<dependency>
    <groupId>cn.javaex</groupId>
    <artifactId>mybatisjj-mysql</artifactId>
    <version>4.1.1</version>
</dependency>
```

#### 特征
##### 1. 轻量
除了 MyBatis，没有任何第三方依赖轻依赖、没有任何拦截器，其原理是通过 SqlProvider 的方式实现的轻实现。同时，在执行的过程中，没有任何的 Sql 解析（Parse）轻运行。 这带来了几个好处：1、极高的性能；2、极易对代码进行跟踪和调试； 3、更高的把控性。

##### 2. 无侵入
只做增强不做改变，引入它不会对现有工程产生影响。不支持hibernate那套查询逻辑，拒绝屎山代码。

##### 3. 支持主键自动生成
支持多达多种主键策略，可自由配置，完美解决主键问题。

##### 4. 灵活
支持 Entity 的增删改查。支持字段填充，可扩展通用SQL方法。


#### 插件文档地址

[https://doc.javaex.cn/mybatisjj-mysql](https://doc.javaex.cn/mybatisjj-mysql)


#### 官网
[https://www.javaex.cn](https://www.javaex.cn)

#### QQ群
587243028

