# mybatisjj-mysql

<div align="center">

![Java](https://img.shields.io/badge/Java-8%2B-blue)
![MyBatis](https://img.shields.io/badge/MyBatis-Enhanced-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-Supported-orange)
![License](https://img.shields.io/badge/License-Apache%202.0-blueviolet)

**一个轻量、无侵入、高性能的 MyBatis 增强工具包**

让常见的增删改查、主键处理、字段填充和通用 SQL 扩展更直接，
在保留 MyBatis 使用习惯的同时，减少重复代码和额外运行负担。

[快速开始](#快速开始) · [项目特性](#项目特性) · [为什么值得试试](#为什么值得试试) · [文档地址](#文档地址)

</div>

---

## 项目简介

`mybatisjj-mysql` 是一个面向 MyBatis 的增强工具。

它的设计重点很明确：

- **足够轻量**：除了 MyBatis，本身不引入额外第三方依赖
- **足够直接**：基于 `SqlProvider` 实现，不依赖拦截器
- **足够高效**：运行过程中不做 SQL Parse，减少额外处理成本
- **足够稳妥**：只做增强，不改变原有工程结构和使用习惯

适合这些场景：

- 已经在使用 MyBatis，希望减少样板 CRUD 代码
- 想保留对 SQL 的掌控力，同时提升开发效率
- 不希望引入重型 ORM 或复杂运行时机制
- 希望在现有项目中低成本接入增强能力

---

## 项目特性

### 轻量

- 除 MyBatis 外，无额外第三方依赖
- 无拦截器机制
- 基于 `SqlProvider` 实现增强能力
- 执行过程中无 SQL Parse

这种实现方式带来的直接收益：

- **运行链路更短**
- **性能开销更低**
- **调试和跟踪更直观**
- **对 SQL 执行过程更容易把控**

### 无侵入

- 只做增强，不改变原有工程结构
- 接入后不会强制替换已有 MyBatis 使用方式
- 更适合在现有项目中逐步接入和迁移

### 支持主键自动生成

- 支持多种主键策略
- 可按业务场景灵活配置
- 能覆盖常见的主键生成需求

### 面向实际开发的增强能力

- 支持 Entity 的增删改查
- 支持字段填充
- 支持扩展通用 SQL 方法

这些能力的目标很直接：

- 减少重复 Mapper 编写
- 降低常规数据操作的模板化劳动
- 让更多代码回到业务本身

---

## 为什么值得试试

很多 MyBatis 项目在迭代过程中都会遇到相似的问题：

- 基础 CRUD 代码重复率高
- 统一字段处理容易散落在各处
- 想增强效率，但又不想引入太重的运行机制
- 想保持 SQL 可控，不希望框架“替你做太多”

`mybatisjj-mysql` 适合的正是这类需求：

- **需要增强，但不想改变项目原本的 MyBatis 结构**
- **需要效率，但不想为此引入额外复杂度**
- **需要通用能力，但不想牺牲灵活性**

它不是为了替代 MyBatis 的使用方式，而是为了让常见开发动作更省力。

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>cn.javaex</groupId>
    <artifactId>mybatisjj-mysql</artifactId>
    <version>5.0.0</version>
</dependency>
```

### 2. 查看文档

- 文档地址：<https://doc.javaex.cn/mybatisjj-mysql>
- 官网：<https://www.javaex.cn>

### 3. 从最常见的场景开始接入

推荐优先从下面这些场景切入：

- 单表基础 CRUD
- 主键策略配置
- 公共字段自动填充
- 通用 SQL 方法沉淀

先解决最常见、最重复的部分，通常更容易快速感受到收益。

---

## 使用体验上的特点

### 保留 MyBatis 的掌控感

对很多团队来说，MyBatis 的价值之一就是 **SQL 可控、调用链清晰、问题定位直接**。

`mybatisjj-mysql` 的增强方式没有走“重封装”路线，而是尽量保留这类体验：

- 看得见执行链路
- 追得上 SQL 来源
- 更容易调试
- 更容易在复杂业务中做定制化处理

### 更适合持续维护

比起“第一次写出来”，很多项目更在意的是：

- 后续是否容易扩展
- 新成员是否容易接手
- 出问题时是否容易定位

轻量、无侵入、少运行时机制，通常意味着更低的维护门槛。

---

## 适合哪些项目

`mybatisjj-mysql` 更适合以下类型的项目：

- Spring Boot / Java Web 后台项目
- 以 MyBatis 为主要持久层方案的业务系统
- 希望增强效率但不想切换开发范式的存量项目
- 对性能、可调试性、SQL 可控性有要求的系统

---

## 文档地址

- 文档：<https://product.javaex.cn/product/mybatisjj>
- 官网：<https://www.javaex.cn>
- QQ 群：`587243028`

---

## License

Apache License 2.0

---

## 一句话概括

**如果需要一个轻量、直接、低接入成本的 MyBatis 增强方案，`mybatisjj-mysql` 值得先跑一遍。**
