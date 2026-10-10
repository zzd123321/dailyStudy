---
description: 以大学 Java MOOC、Java 官方教材和中文开源教程为参考，先完成 Java 与数据库，再学习 Spring Boot、Python 和 AI 应用开发。
---

# 学习路线与参考教材

目前先学 Java。你有前端经验，HTTP 调用和页面开发可以少花一些时间；Java 的类型、对象、集合、异常和数据访问需要从头学。Python 等 Java 后端能够独立运行以后再开始。

整个项目采用同一个业务场景：用户管理任务和文档，后续增加文档问答。开始时是命令行程序，接着接数据库和网页，最后接入模型。每个版本都应该能独立使用。

## 教材怎么选

本次在 GitHub 搜索了 Java 教程，并阅读了下面资料的目录与相关章节。没有统一的“教学评分”可以直接比较它们；星数表示社区关注度，适合入门与否还要看内容。以下星数为 2026 年 10 月 9 日搜索页面所见的约数，不是课程评分。

| 资料 | 适合做什么 | 本课程怎样使用 |
|---|---|---|
| [赫尔辛基大学 Java Programming MOOC](https://java-programming.mooc.fi/) · [教材源码](https://github.com/rage/java-programming/tree/master/data) | 从变量、输入、循环到对象，每个概念配小练习 | 参考知识递进和练习形式。它采用的旧工具配置不照搬，示例统一使用 Java 21 |
| [Java 官方学习文档](https://dev.java/learn/) · [官方源码](https://github.com/java/devjava-content) | 核对类型、对象、数组与程序执行规则 | 语言规则以官方说明为准；英文资料按主题选读 |
| [二哥的 Java 进阶之路](https://github.com/itwanger/toBeBetterJavaer)，约 1.77 万星 | 中文解释和独立知识点 | 用作补充阅读，按章节链接阅读，不要求通读整站 |
| [JavaGuide](https://github.com/Snailclimb/JavaGuide)，约 15.9 万星 | 知识归纳、面试复习、后端专题 | 基础学完后查漏补缺，不用面试问答代替第一次学习 |
| [Spring 官方 Guides](https://github.com/spring-guides) | 一个指南完成一个小功能 | Java 与 SQL 学完以后，从 REST 服务和数据库访问开始 |
| [MDN HTTP 教材](https://developer.mozilla.org/zh-CN/docs/Web/HTTP) · [文档源码](https://github.com/mdn/content) | HTTP 消息、状态码、Fetch 与浏览器行为 | Web 专题的主要参考 |

这里的正文是根据这些资料重新组织的讲解和本仓库的例子，不是外部教材的转载或逐段翻译。每篇给出对应章节，读完正文后可以用它们补充不同例子。

## 已有内容的阅读顺序

原来的顺序在 Java 语法之间插入了 HTTP，现在改为连续的 Java 基础，以及单独的 Web 专题。**页面地址和已有示例源码路径保持可用，正文编号按新目录调整。** 已经学过的部分可以直接复习，不需要重新做一遍。

| 新目录 | 内容 | 对应旧课 |
|---|---|---|
| [Java 01：程序结构、编译与运行](/lessons/java-toolchain) | class、main、变量、编译、classpath，包与 Maven 放在附录 | 旧 01 |
| [Java 02：基本语法、数组与方法](/lessons/java-control-flow) | 类型、运算、字符串、分支、循环、数组、参数与返回值 | 旧 03 |
| [Java 03：类、对象与封装](/lessons/java-objects) | 字段、构造器、实例方法、引用、static 与状态修改 | 旧 05 |
| [Java 04：继承、接口与多态](/lessons/java-collaboration) | extends、super、重写、抽象类、implements、组合 | 旧 06 |
| [Java 05：集合与任务管理](/lessons/java-collections) | List、Set、Map、过滤、删除与集合复制 | 新增 |
| [Java 06：泛型、包装类型与对象相等](/lessons/java-generics-equality) | 类型检查、装箱与拆箱、equals/hashCode、稳定的任务键 | 新增 |
| [Web A：HTTP 请求与响应](/lessons/http) | 用 curl 和 Network 观察请求，处理 JSON 与错误 | 旧 02 |
| [Web B：请求处理与数据保存](/lessons/request-lifecycle) | 服务进程、路由、校验、内存与文件 | 旧 04 |

Java 01–06 按顺序阅读。Web A、B 可以在开始 Spring Boot 前阅读；它们不作为学习 Java 对象的前置条件。

## 第一阶段：Java 基础

已发布的是前六章，后面的主题会继续加入同一网站。下面包含已有章节与后续顺序，07 起的正文尚未发布。

| 顺序 | 主要内容 | 要写出的代码 |
|---|---|---|
| 01–04 | 程序、语法、对象、继承与接口 | 能创建任务，修改状态，使用不同格式显示它 |
| [05](/lessons/java-collections) | List、ArrayList、Set、Map、HashMap、LinkedHashMap；遍历与查找 | 添加、查询、完成、删除多条任务 |
| [06](/lessons/java-generics-equality) | 泛型、包装类型、equals 与 hashCode | 正确使用集合，解释对象相等与重复元素 |
| 07 | 异常、调用栈、try/catch、异常传播 | 区分错误输入、业务失败和程序缺陷 |
| 08 | Path、Files、字符编码、JSON、资源关闭 | 保存任务，退出后重新读取 |
| 09 | LocalDate、Instant、时区、枚举 | 表达截止日期与任务状态 |
| 10 | lambda、函数式接口、Stream | 筛选和统计任务，比较循环与 Stream 的写法 |
| 11 | Maven、依赖、JUnit、调试 | 为状态规则和文件保存写测试，并打包程序 |
| 12 | 线程、共享数据、线程池、同步 | 重现并修复多个线程修改同一份数据的问题 |

集合先学习怎样使用，再学习泛型的完整规则。异常先理解失败怎样向上传递，再设计错误类型。反射、注解实现机制和 JVM 调优等到框架或性能问题中再学。

Java 基础结束时，任务管理器应支持新增、修改、完成、查找和文件保存。验收时换一组数据运行，故意输入错误值，重启程序检查文件恢复。能解释结果，比把教材的代码复制成功更有意义。

主要阅读：[MOOC Part 1–6](https://github.com/rage/java-programming/tree/master/data)、[Javaer 基础与对象章节](https://github.com/itwanger/toBeBetterJavaer#java基础)。MOOC 中的 JavaFX 桌面界面不作为这条路线的必修。

## 第二阶段：SQL、PostgreSQL 与 JDBC

先在数据库中写 SQL，再让 Java 执行 SQL。

学习次序是表、数据类型和约束 → INSERT/SELECT/UPDATE/DELETE → 条件与排序 → 主外键与 JOIN → 分页 → 事务 → 索引与执行计划。业务表从任务开始，再增加用户和文档。

Java 端接着学习 Connection、PreparedStatement、ResultSet 和资源关闭。参数使用占位符传入，不把用户输入拼进 SQL。一次操作需要修改多张表时，明确事务从哪里开始、在哪里提交或回滚。

这一阶段将文件版任务管理器改为数据库版。验证重启以后仍能查询任务，插入非法数据会受到约束，事务失败不会留下半份修改。

资料：[PostgreSQL 官方教程](https://www.postgresql.org/docs/current/tutorial.html)、[SQLBolt](https://sqlbolt.com/)、[Spring 官方 JDBC 示例](https://github.com/spring-guides/gs-relational-data-access)。JDBC 示例先看普通 Java 数据访问，不急着引入 ORM。

## 第三阶段：Spring Boot 与业务后端

先读 Web A、B。然后按下面的功能学习 Spring，而不是先把所有框架原理背一遍：

1. 创建 REST 服务：Controller、请求参数、请求体、响应对象。
2. 处理输入：校验、错误响应、HTTP 状态码。
3. 组织代码：依赖注入、Service、配置、日志。
4. 访问数据库：先选 JDBC 或 MyBatis 一条路线，加入迁移和事务。
5. 完成业务：任务和文档 CRUD、分页、上传。
6. 登录与权限：身份认证、会话或令牌、资源所属用户、RBAC。
7. 接已有前端：联调、跨源、错误提示、请求取消。
8. 测试与部署：接口测试、Docker Compose、Nginx、环境配置、备份与恢复。

OAuth2、PKCE 和 OIDC 在接第三方身份系统时学习，分别弄清授权与认证。Redis 从一个确实需要缓存的查询开始。Spring Cloud、注册中心、分布式事务和 Kubernetes 留到单体服务可交付以后。

这一阶段结束时，别人应该能用你的网页注册或登录，管理自己的任务和文档；不同用户不能仅靠更换 ID 访问对方的数据。项目同时有自动化测试、数据库迁移和部署说明。

资料：[Spring REST 入门](https://github.com/spring-guides/gs-rest-service)、[Spring JDBC 入门](https://github.com/spring-guides/gs-relational-data-access)、[Spring Security 示例](https://github.com/spring-guides/gs-securing-web)。项目建立时选择兼容 Java 21 的稳定版本，固定依赖，不直接使用浮动版本。

## 第四阶段：Python 与 FastAPI

这时已经理解接口、对象和 SQL，Python 可以集中学习语言差异：列表与字典、函数、作用域、模块、异常、类型标注、虚拟环境、包管理，再补迭代器、生成器、上下文管理器和装饰器。

用 Python 处理同一批文档：读取 JSON，清洗文本，分段，统计长度，输出处理结果。随后用 FastAPI 把文档处理函数变为接口，学习 Pydantic、配置、日志、pytest 和异步请求。

先让 Java 调用一个返回固定结果的 Python 服务，检查请求字段、超时和失败处理，再替换为模型调用。业务用户、任务和权限继续由 Java 管理，Python 主要处理模型与检索。

资料：[Python 官方教程](https://docs.python.org/zh-cn/3/tutorial/)、[Python-100-Days](https://github.com/jackfrued/Python-100-Days)、[FastAPI](https://fastapi.tiangolo.com/zh/tutorial/)。元类、描述符与复杂包发布不是第一次写 AI 接口的前置条件。

## 第五阶段：大模型应用

先学模型 API，不先训练模型。理解 token、消息角色、上下文长度、输入输出成本，完成普通响应、流式响应和结构化输出。再学习提示词、超时、重试、取消与速率限制。

第一个功能做文档摘要：上传文档，返回摘要和待办事项，页面显示生成过程。密钥留在服务端，日志避免记录敏感正文。为固定文档准备可人工检查的结果，观察每次修改对输出、延迟与成本的影响。

模型原理学习到能解释嵌入、注意力、Transformer、训练与推理的差别。NumPy、Pandas 和绘图工具用于具体实验，不把完整数据科学课程都设为 API 调用的前提。

资料：[Microsoft 生成式 AI 入门](https://github.com/microsoft/generative-ai-for-beginners)、[Hugging Face LLM Course](https://huggingface.co/learn/llm-course/)、所选模型供应商的官方 API 文档。

## 第六阶段：RAG 文档问答

顺序是文档清洗 → 分块与元数据 → embedding → 相似度检索 → 拼接上下文 → 生成答案与引用 → 评估。

先用一组小文档把整个过程跑通，再尝试关键词检索与向量检索的组合、重排序和缓存。每个检索结果都保留来源与访问权限；回答不知道的问题时，允许拒答，而不是强行生成内容。

准备一份测试问题集，检查是否找到了正确段落、回答有没有依据、引用能否回到原文。GraphRAG 与多模态检索等基础效果稳定后再选学。

资料：[Datawhale llm-universe](https://github.com/datawhalechina/llm-universe)、[LlamaIndex 文档](https://docs.llamaindex.ai/)。

## 第七阶段：工具调用与 Agent

先写一个受控工具调用循环，理解模型提出调用、程序执行工具、结果返回模型的过程，再学习 LangGraph 的状态、节点、边、检查点、中断与恢复。

项目功能选“从文档提取任务，经用户确认后写入任务系统”。只读查询和写入操作分开；写入仍走 Java 的认证、权限与业务规则，不能让模型直接绕过它们访问数据库。

然后学习 MCP、工具发现、调用限制、失败重试和评估。多 Agent、Deep Agents、复杂沙箱等作为后续专题，不把工具数量当成项目成熟度。

资料：[LangGraph 官方文档](https://docs.langchain.com/oss/python/langgraph/overview)、[MCP 官方规范](https://modelcontextprotocol.io/)、[Microsoft AI Agents 入门](https://github.com/microsoft/ai-agents-for-beginners)。

## 第八阶段：交付与求职准备

将前端、Java、Python、PostgreSQL 和模型访问组成可部署系统。完成自动化构建与测试、配置管理、日志与指标、模型调用追踪、错误告警、备份恢复和回滚演练。

简历以一个完整项目为主：说明使用场景、数据模型、权限设计、检索评估结果、失败处理与部署方式。面试准备结合自己写过的代码，再用 JavaGuide 补查 Java、数据库、网络和并发知识。

大模型微调、Dify、Spring Cloud、高可用集群与分库分表保留为岗位拓展。是否加入，要看目标岗位和项目遇到的问题。

渡一目录继续作为主题检查表：[原目录](https://app.duyiedu.com/toc-detail?productId=54a002b2-1227-4207-9aed-e75be9cdb590)。它列出的 OAuth2、RBAC、数据库、部署和 Agent 评估都保留；微服务、微调和复杂平台工程后置。学习顺序不再按三十门课逐门排列。
