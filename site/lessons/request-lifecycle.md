---
description: 观察一次请求如何经过路由、解析、校验与存储，并通过重启和文件读写区分页面状态、服务内存与持久化数据。
---

# Web B · 请求处理与数据保存

在 Web A 中，我们发送 POST 创建笔记，再发送 GET 查询。现在看请求进入 Java 程序以后发生的事情，以及为什么刷新页面和重启服务的结果不同。

使用同一个实验服务，代码在 `projects/http-playground`，启动方式见 [Web A](/lessons/http#启动实验服务)。主要参考 [MDN 的客户端/服务器说明](https://github.com/mdn/content/blob/main/files/en-us/web/http/guides/overview/index.md)和 [Spring 官方 REST 示例](https://github.com/spring-guides/gs-rest-service)。这里先观察普通 Java 服务，Spring 实现留到后续阶段。

## 服务进程

前面的 Hello 程序执行 main，打印后结束。Web 服务启动后需要持续运行，才能接收以后的请求。

实验 main 调用 start 创建服务、监听本机端口并注册处理逻辑。后续请求由 HTTP 服务分派到处理方法，不会每次从头重跑 main。

浏览器里的 JavaScript 与 Java 服务不是同一个程序。前端不能直接读取 Java 的变量，它通过请求获取服务返回的数据。

```text
浏览器页面
    │ HTTP 请求
    ▼
Java 服务进程
    │ 查找或修改内存中的笔记
    ▼
HTTP 响应 → 页面更新显示
```

## 路由

路由根据方法与路径选择处理逻辑。本服务主要有三种：

| 请求 | 操作 |
|---|---|
| GET /api/notes | 查询列表 |
| POST /api/notes | 创建笔记 |
| GET /api/notes/某个ID | 查询详情 |

概念上可以写成：

```java
if (path.equals("/api/notes")) {
    if (method.equals("GET")) {
        // 查询列表。
    } else if (method.equals("POST")) {
        // 创建笔记。
    } else {
        // 返回 405。
    }
}
```

这段是讲解片段，不是另一个完整服务。Spring 后续使用 @GetMapping、@PostMapping 等声明路由，解决的是同一个选择问题。

### 两种 404

GET /api/unknown 没有匹配接口；GET /api/notes/999999 匹配了详情接口，但找不到对应笔记。二者都返回 404，发生的位置不同，错误码可以帮助区分。

找到接口也不代表业务成功，还需要检查输入与业务数据。

## 解析、校验与修改

创建请求的实际顺序如下：

1. 检查 Content-Type 是否符合 JSON 要求。
2. 把正文解析为 JSON 数据。
3. 检查 title 是否是字符串、去空白后是否有效。
4. 生成 ID，保存笔记。
5. 将结果编码为 JSON，返回 201。

对应 Web A 的错误实验：媒体类型不符返回 415，解析失败返回 400，标题不合法返回 422。前三种失败都发生在保存之前。

这与 Java 对象中的 rename 顺序相似：新值检查通过后才修改现有状态。错误不应先造成修改，再靠前端猜测怎样撤回。

### 请求推演

选择场景可以观察服务在哪一步返回。它是根据本实验源码编写的演示，不会从网站发起实际创建：

<RequestFlow />

模拟 500 只是主动返回一个错误，用于区分错误响应和连接失败，没有真的启动数据库或调用模型。

## 重启实验

### 创建并查询

在新启动实例上，先查询列表，再创建一条：

```bash
curl -s 'http://127.0.0.1:8082/api/notes'
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":"测试数据保存"}'
curl -s 'http://127.0.0.1:8082/api/notes'
```

以返回的 total 与 ID 为准。创建成功后，列表应多一条。

### 刷新页面

刷新实验页面，再发 GET。服务进程没有停止，所以笔记仍然存在。

刷新丢失的是页面当前 JavaScript 内存，例如尚未提交的输入。提交后保存到服务的数据属于另一个进程，刷新不会直接删除它。

### 停止服务

回到启动服务的终端，按 Ctrl+C，随后在请求终端执行：

```bash
curl -i --connect-timeout 2 --max-time 5 'http://127.0.0.1:8082/api/notes'
```

目标端口没有服务监听时，会连接失败。这里没有返回 404 或 500。

### 再次启动

再次执行 Web A 的 java 启动命令，再查询列表。会恢复为初始笔记，本次创建的笔记丢失，因为本服务只把它保存在内存。

| 操作 | 当前页面内存 | 服务内存中的笔记 |
|---|---|---|
| 刷新页面 | 重新建立 | 仍在，只要服务未停止 |
| 关闭页面 | 页面状态结束 | 仍在，只要服务未停止 |
| 停止并重启服务 | 可以重新请求 | 旧实例内存丢失，加载初始数据 |

数据库或文件能够跨进程保存数据，但前提是程序确实写入了它们。项目里存在一个数据库，并不代表所有变量都会自动被保存。

## 文件保存实验

这部分是独立选读，用来观察跨进程保存。它不会把 HTTP 实验服务变成文件版服务。Files 的完整用法在 Java 文件章节继续学习。

在独立目录保存 `SavedNote.java`：

```java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SavedNote {
    public static void main(String[] args) throws IOException {
        Path file = Path.of("note.txt");
        if (args.length == 1) {
            Files.writeString(file, args[0], StandardCharsets.UTF_8);
            System.out.println("已写入文件。");
        } else if (args.length == 0 && Files.exists(file)) {
            System.out.println(Files.readString(file, StandardCharsets.UTF_8));
        } else {
            System.out.println("没有可读取的笔记，或参数数量不正确。");
        }
    }
}
```

运行：

```bash
javac -encoding UTF-8 SavedNote.java
java SavedNote "退出程序后仍要保留"
java SavedNote
```

第一条 java 命令启动一个进程，写文件后退出。第二条启动另一个进程，从文件读取，输出“退出程序后仍要保留”。数据来自文件，不是上个进程的变量。

相对路径 note.txt 根据当前工作目录解析。换目录运行可能读到另一个位置，先检查工作目录，不能仅因文件名相同就认定是同一份文件。

writeString 在本例中覆盖已有内容。它没有并发控制、多个记录管理或事务，适合观察机制；任务量增大后会学习数据库。

## HTTP 无状态与业务数据

HTTP 无状态不是“服务器不能保存数据”。它表示协议不会自动为每次请求保留并恢复上一次业务上下文。

本服务仍然在内存中保存笔记。以后登录系统可能用 Cookie 携带会话标识，让服务找到用户；数据库保存任务，缓存保存临时结果。这些机制由应用另外建立。

不要把页面状态、服务内存、数据库记录和 HTTP 会话当成同一个东西。

## 输入对象、业务对象与响应对象

请求只提交 title，存储的笔记还有 id，响应又可能增加 items、total 等字段。不同环节需要的数据并不完全相同。

项目里用 JSON 库把 Java 数据编码为响应。字符串拼接并不是通用 JSON 序列化：标题含双引号、反斜线或换行时，需要正确转义。

同样，不能因为用户对象有密码摘要字段，就把所有字段都直接返回前端。哪些字段属于响应，是 API 的明确设计。

### 代码分工

服务功能多了以后，通常拆成：

| 部分 | 主要负责 |
|---|---|
| 请求入口 / Controller | HTTP 参数、校验、状态与响应 |
| 业务逻辑 / Service | 操作规则，调用其他能力 |
| 数据访问 / Repository | 保存与查询 |

这是后续 Spring 项目的组织方式。当前实验为了集中展示流程，仍放在一个类中，不假装已经有三层框架。

## 提交成功但页面提示失败

假设服务已保存笔记并发出响应，连接随后中断，浏览器没拿到结果。页面看到失败，不一定表示服务没有修改数据。

因此遇到提交失败，不要直接宣布“肯定没保存”。可以重新查询，或者按设计好的请求标识判断操作结果。重复发送相同 POST，在本服务里会再次创建。

幂等键、事务、并发修改与可靠消息会在相应专题学习。现在先识别问题：服务执行结果与客户端获知结果之间，存在可能失败的传输过程。

## 排查方法

先记录请求方法、路径、状态、错误正文，再确定问题发生在哪一步：

| 观察 | 优先检查 |
|---|---|
| Network 中没有实际请求 | 页面事件、前端代码、客户端校验 |
| 连接失败 | 地址、端口、服务是否运行 |
| 404 / 405 | 路径、ID、方法、路由 |
| 400 / 415 / 422 | 查询参数、媒体类型、JSON、字段要求 |
| 201 但列表显示旧内容 | 新请求结果、页面状态更新、保存位置 |
| 重启后数据恢复初始值 | 是否只保存在内存 |

状态码只能缩小范围，具体原因继续看错误码和日志。服务端日志记录请求相关信息时，应避免把密码、令牌或完整敏感正文直接输出。

## 练习

### 1. 非法创建

先记下 total，提交空白标题，再查询 total。它应该变化吗？在源码中找到数据写入之前的检查。

::: details 答案
不变化。title 校验在分配 ID 和写入之前。实验代码在 createNote 方法中完成这些步骤。
:::

### 2. 页面与进程

创建后只刷新页面，与创建后重启服务，结果为什么不同？

::: details 答案
刷新重新建立页面，服务还在；重启结束了保存数据的服务实例。当前没有文件或数据库恢复逻辑，所以重新载入初始笔记。
:::

### 3. 文件位置

SavedNote 在目录 A 写入，在目录 B 运行读取，为什么可能提示没有笔记？

::: details 答案
Path.of 使用相对路径，分别指向 A/note.txt 与 B/note.txt。工作目录是文件定位的一部分。
:::

### 4. 重复 POST

页面显示提交失败，随后查询已存在该标题。能否据此认为以后所有同标题提交都应该合并？

::: details 答案
不能。两个合法笔记也可能同标题，按标题去重会改掉业务规则。要支持安全重试，另外设计操作标识、唯一约束或幂等机制。
:::

## 对应阅读

- [MDN：HTTP Overview](https://github.com/mdn/content/blob/main/files/en-us/web/http/guides/overview/index.md)：客户端、服务器与无状态。
- [MOOC：分离界面和程序逻辑](https://github.com/rage/java-programming/blob/master/data/part-6/2-separating-user-interface-from-program-logic.md)：先用命令行例子理解分工。
- [Spring REST 指南](https://github.com/spring-guides/gs-rest-service)：后续核对 Controller 与响应数据。
- [Spring JDBC 指南](https://github.com/spring-guides/gs-relational-data-access)：学 SQL 后再将数据保存到数据库。

后续课程安排见[学习路线](/learning-path)。
