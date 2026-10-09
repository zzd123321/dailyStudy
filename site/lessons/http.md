---
description: 通过本地笔记接口学习 URL、HTTP 消息、方法、状态码、JSON、Fetch 错误处理，以及跨源与缓存的基本区别。
---

# Web A · HTTP 请求与响应

HTTP 规定客户端和服务器怎样交换请求与响应。浏览器使用它获取页面，前端代码也使用它读取和提交业务数据。Java 类里的方法调用发生在程序内部，HTTP 请求则发给一个网络地址上的服务。

本篇参考 [MDN：HTTP 概述](https://github.com/mdn/content/blob/main/files/en-us/web/http/guides/overview/index.md)和[HTTP 消息](https://github.com/mdn/content/blob/main/files/en-us/web/http/guides/messages/index.md)。使用仓库现有笔记服务观察真实消息，不要求先读懂它所有 Java 代码。

## 启动实验服务

使用已有的 Java 21 与 Maven，在仓库根目录执行：

```bash
mvn -B -ntp -f projects/http-playground/pom.xml test dependency:copy-dependencies
java -cp 'projects/http-playground/target/classes:projects/http-playground/target/dependency/*' com.dailystudy.day002.HttpPlayground
```

第一条编译、运行测试并准备依赖；第二条启动服务。保持这个终端运行，另开一个终端发送请求。实验页面地址为 `http://127.0.0.1:8082/`。

服务只监听本机，浏览器和服务要在同一台电脑。数据保存在内存，重启会恢复初始笔记；它没有用户认证功能。

端口被占用时，在启动命令最后加 8083，后续请求也一起改用 8083。不要为运行示例结束不认识的进程。

## URL

```text
http://127.0.0.1:8082/api/notes?limit=2
```

| 部分 | 含义 |
|---|---|
| http | 协议方案 |
| 127.0.0.1 | 主机，这里是当前电脑 |
| 8082 | 目标端口 |
| /api/notes | 请求路径 |
| limit=2 | 查询参数 |

端口帮助找到这台电脑上提供服务的程序，路径帮助服务判断要处理的资源。路径相同，方法不同，也可能有不同操作。

URL 中的 `#section` 是片段，通常供浏览器使用，不作为请求目标发送给服务。远程服务一般使用 HTTPS，通过 TLS 保护传输。

## 查询笔记

```bash
curl -i 'http://127.0.0.1:8082/api/notes?limit=2'
```

curl 默认发送 GET。`-i` 同时显示响应头与正文。刚启动服务时，关键内容类似：

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8
Cache-Control: no-store

{"items":[{"id":1,"title":"第1天：Java工具链"}],"total":1}
```

响应分三部分：状态行、响应头、正文。空行隔开头和正文。

200 表示这次请求成功；Content-Type 表示正文是 JSON；items 是当前返回的笔记，total 是总数。limit=2 只限制返回数量，不会创建笔记，所以总共只有一条时仍只返回一条。

在实验页面点击发起请求，然后在浏览器开发者工具的 Network 面板查看 Headers 与 Response。页面里的 JavaScript 与 curl 使用同一个 API，观察结果应该能对应起来。

## 创建笔记

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":"阅读 HTTP 教材"}'
```

POST 表示此接口的创建操作，Content-Type 告诉服务按 JSON 解读正文，--data 提供正文。行尾反斜线是 shell 的续行语法，后面不要再加空格。

第一次创建时，响应通常包含：

```http
HTTP/1.1 201 Created
Content-Type: application/json; charset=utf-8
Location: /api/notes/2

{"id":2,"title":"阅读 HTTP 教材"}
```

ID 由服务生成，以自己的返回值为准。再用该 ID 查询：

```bash
curl -i 'http://127.0.0.1:8082/api/notes/2'
```

创建请求提供 title，响应返回 id 和 title，二者不是同一份数据。Location 是新资源的位置，201 表示创建完成。

### 请求消息

这次请求的 HTTP/1.1 示意如下，省略长度等头：

```http
POST /api/notes HTTP/1.1
Host: 127.0.0.1:8082
Content-Type: application/json
Accept: application/json

{"title":"阅读 HTTP 教材"}
```

请求行包含方法与请求目标，接着是请求头，空行后是请求正文。HTTP/2、HTTP/3 的线编码不同，这里用 HTTP/1.1 展示结构，但方法和状态等语义仍适用。

Content-Type 表示当前消息的正文格式。请求中的 Accept 表示希望收到的响应格式，不保证服务一定支持。设置 Content-Type 不会自动修复无效 JSON。

## 方法与接口约定

| 方法 | 通常的资源语义 | 本实验是否实现 |
|---|---|---|
| GET | 读取 | 列表与详情 |
| POST | 提交处理，常用于创建 | 创建笔记 |
| PUT | 替换指定资源 | 未实现 |
| PATCH | 部分修改 | 未实现 |
| DELETE | 删除指定资源 | 未实现 |

方法、地址、字段要求和结果共同构成接口约定。REST 的设计建议不意味着服务器自动支持所有方法，先看具体 API。

此实验只要求 title 是字符串，去首尾空白后非空，最多 120 个 Unicode 码点。列表的 limit 是 1–20 的整数。客户端与服务器必须对这些要求达成一致。

GET 被定义为安全方法，不应承载创建等状态修改意图。PUT、DELETE 等具有幂等语义；POST 创建不默认幂等。在本服务中重复发送同样的 POST，会创建两条不同 ID 的笔记。

## 查询参数与编码

参数值包含空格、中文或 & 时，应先编码。浏览器代码使用 URLSearchParams：

```javascript
const params = new URLSearchParams({ limit: '2' });
const url = `/api/notes?${params.toString()}`;
```

curl 可以让工具编码查询值：

```bash
curl -i --get 'http://127.0.0.1:8082/api/notes' \
  --data-urlencode 'limit=2'
```

本实验没有实现关键词搜索或 offset 分页。不要仅往 URL 加一个 search 参数，就假定服务已经支持搜索。

## 状态码与错误正文

分别发送下面三种错误请求：

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: text/plain' --data '{"title":"HTTP"}'

curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' --data '{"title":'

curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' --data '{"title":"   "}'
```

依次得到 415、400、422：媒体类型不支持，JSON 语法错误，字段值不满足要求。

| 结果 | 在本服务中的含义 |
|---|---|
| 200 | 查询成功 |
| 201 | 创建成功 |
| 400 | JSON 或查询参数不合法 |
| 404 | 路径或笔记不存在 |
| 405 | 路径支持的方法与本次方法不符，另有 Allow 头 |
| 415 | 请求正文媒体类型不支持 |
| 422 | JSON 能解析，但标题不符合要求 |
| 500 | 服务端错误，或本实验主动模拟的错误 |

错误正文包含 code 与 message。状态码概括结果，错误码解释这个 API 的具体原因。不同 API 对校验失败可能采用不同状态约定，不要把 422 当成所有系统必须使用的规则。

服务停止时，客户端连接失败，没有服务器返回的 HTTP 状态码。连接失败和收到 500 是不同问题。

## Fetch 的错误处理

fetch 收到 404、500 时，通常仍返回 Response，不会自动进入 catch。需要检查 response.ok，再处理正文。下面可在实验页面的控制台运行：

```javascript
async function createNote(title) {
  const response = await fetch('/api/notes', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title })
  });
  const data = await response.json(); // 本实验 API 的成功和错误都返回 JSON。
  if (!response.ok) {
    throw new Error(data.error?.message ?? `HTTP ${response.status}`);
  }
  if (!Number.isInteger(data.id) || typeof data.title !== 'string') {
    throw new Error('响应字段不符合预期');
  }
  return data;
}
```

测试：

```javascript
try {
  const note = await createNote('练习 Fetch');
  console.log(note);
} catch (error) {
  console.error(error.message);
}
```

把标题改为三个空格，会显示业务错误。如果服务器停止，fetch 会因网络错误拒绝 Promise。若别的 API 返回空正文或 HTML，不能不加判断就调用 json；此函数仅针对本实验的 JSON 契约。

读取正文也是异步操作。Response 正文是流，通常不能对同一响应依次调用 json 和 text 来重复读取；需要两种读取用途时先考虑 clone。

## 跨源与缓存

这两个话题是浏览器行为，初次阅读先分清概念，细节可以在前后端联调时继续查 MDN。

### 跨源

源由协议、主机、端口组成。网站与实验服务端口不同，即使都在当前电脑，也可能跨源。

跨源 application/json POST 通常触发 OPTIONS 预检，浏览器询问目标服务是否允许相应来源、方法和头。本实验没有配置跨源支持，先在它自己的页面调用 API，使用同源请求。

curl 不执行浏览器 CORS 检查，所以 curl 成功不保证浏览器能读取响应。mode: 'no-cors' 也不是绕过读取限制的办法。

### 缓存

响应可以使用 Cache-Control 指定缓存策略。本实验使用 no-store，避免笔记实验受到缓存干扰。

缓存内容仍新鲜时，浏览器可能直接使用缓存；需要验证缓存时，也可能发出条件请求并收到 304。304 没有新的资源正文，客户端继续使用已有缓存。no-cache 与 no-store 含义不同：前者允许存储但要求验证，后者要求不存储。

浏览器显示旧内容时，还可能是前端状态没有更新，不能只看“没变化”就认定是 HTTP 缓存。

## 练习

### 1. 请求数量

创建三条笔记后执行 limit=2。items 最多有几条？total 应该是多少？

::: details 答案
如果从新启动实例开始，原有一条，创建三条后 total 是 4，items 最多两条。limit 限制本次返回项，不改变总数。
:::

### 2. 错误分类

分别请求未知路径、未知笔记 ID、POST 空白标题，以及停止服务后的正常路径。判断有没有 HTTP 响应，原因分别是什么。

::: details 答案
前三者有响应，分别为路由 404、资源 404、字段校验 422。停止服务后，目标端口若无监听者，是连接失败，没有这个服务返回的状态码。
:::

### 3. 重复提交

连续发送两次同样的合法 POST，再查询列表。为什么标题相同仍得到两个 ID？

::: details 答案
本实验创建接口没有去重或幂等键机制，两次提交分别创建两条记录。禁用按钮只能减少某些前端重复操作，不等于服务器实现了幂等。
:::

### 4. 响应检查

只写 await response.json、不检查 response.ok，有什么问题？response.ok 为 true 是否就能证明返回字段正确？

::: details 答案
错误响应也可能是合法 JSON，解析成功不代表业务成功。ok 只表示 HTTP 状态在 200–299 范围，字段类型和业务含义仍按 API 契约检查。
:::

## 对应阅读

- [MDN：HTTP 概述](https://github.com/mdn/content/blob/main/files/en-us/web/http/guides/overview/index.md)：客户端、服务器、连接与无状态。
- [MDN：HTTP 消息](https://github.com/mdn/content/blob/main/files/en-us/web/http/guides/messages/index.md)：请求行、头和正文。
- [MDN：Using Fetch](https://github.com/mdn/content/blob/main/files/en-us/web/api/fetch_api/using_fetch/index.md)：Promise、状态检查和正文处理。
- [Spring 官方 REST 示例](https://github.com/spring-guides/gs-rest-service)：学到 Spring 后再阅读 Controller，不作为本篇前置内容。

[继续 Web B：请求处理与数据保存](/lessons/request-lifecycle)。
