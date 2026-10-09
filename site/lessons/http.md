---
description: 拆解 HTTP 请求与响应，用浏览器和 curl 理解接口契约、状态码、输入校验与故障诊断。
---

# 02 · HTTP 请求、响应与诊断

前端调用接口时，一次 fetch 背后发生了什么？本课从服务端视角拆解一次 HTTP 交换，并通过可以实际运行的 Java 服务复现成功和失败。

重点是理解协议与诊断方法。服务内部用到的集合、HttpServer 和测试框架会在后续编程课程展开；这里先把它作为实验工具。

## 从输出文字到返回响应

第 01 课 中，main 计算结果并打印到终端。本课 main 启动一个等待请求的进程：

```text
浏览器 / curl  ── GET /api/notes?limit=2 ──→  Java 服务
浏览器 / curl  ←── 200 + 响应头 + JSON ────  Java 服务
                                           路由 → 参数校验 → 查询内存
```

客户端发出请求，服务端根据接口约定处理，再返回响应。服务运行在哪里、当前有什么数据、如何校验输入，都影响最终结果。

本实验的条目保存在内存，重启恢复初始数据。持久化会在数据库阶段学习。


## 一次交换包含什么

请求要看：**方法、URL、请求头、请求正文**。响应要看：**状态码、响应头、响应正文**。

下面是一个创建请求的 HTTP/1.1 文本示例：

```http
POST /api/notes HTTP/1.1
Host: 127.0.0.1:8082
Content-Type: application/json
Accept: application/json
Content-Length: 22

{"title":"HTTP notes"}
```

对应响应的关键部分可能是：

```http
HTTP/1.1 201 Created
Content-Type: application/json; charset=utf-8
Location: /api/notes/2

{"id":2,"title":"HTTP notes"}
```

响应示例省略了其他自动生成的头。示例中的数字 ID 对应新启动服务的首次创建；实际操作以返回结果为准。你不用手写 Content-Length，curl 或浏览器会计算它；它表示正文的字节数，包含中文时不能直接用字符个数代替。

空行用于分隔头部和正文。HTTP/2、HTTP/3 的传输编码不等同于这段文本，但方法、头、状态码等概念仍然适用，本课先掌握这些概念。

### URL 的各部分

```text
http://127.0.0.1:8082/api/notes?limit=2#results
│      │         │    │         │       └─ fragment：通常供客户端使用，不发送给服务器
│      │         │    │         └─ query：查询参数
│      │         │    └─ path：接口路径
│      │         └─ port：端口
│      └─ host：本机回环地址
└─ scheme：协议方案
```

本机实验用 HTTP；远程服务常用 HTTPS，为 HTTP 交换增加 TLS 保护。后续网络课程再展开 TLS。

在本实验中：

- `/api/notes/1` 的 `1` 位于路径，用于标识一条资源。
- `?limit=2` 位于查询部分，控制列表最多返回多少条。
- POST 的 JSON 正文包含待创建数据。
- URL 中写了 `title` 不代表服务就会读取它；接口必须明确约定从哪里获取输入。

### 三个值得区分的头

| 头 | 意思 | 示例 |
|---|---|---|
| 请求 Content-Type | 我发送的正文是什么格式 | application/json |
| 请求 Accept | 我希望收到什么格式 | application/json |
| 响应 Content-Type | 实际返回的正文是什么格式 | application/json; charset=utf-8 |

设置 Content-Type 不会自动把普通字符串转换成 JSON。Accept 也不保证服务支持所有格式；本实验 API 固定返回 JSON。

HTTP 头名不区分大小写，所以工具显示 Content-type 或 content-type 时，仍是同一个头。


## 接口契约：前后端共同遵守的输入输出规则

接口不只是一个 URL。方法、输入位置、类型、校验规则、成功结果和失败语义共同组成契约。

以创建知识条目为例：

```text
方法与路径：POST /api/notes
请求类型：application/json
正文：title，非空字符串，最多 120 个字符
成功：201，返回 id 与 title，Location 指向新条目
失败：415 表示媒体类型不符；400 表示正文不能解析；422 表示字段规则不符
```

前端表单中的 `required` 能改善体验，但请求可以由 curl、其他客户端或修改过的页面发送。后端必须对实际收到的数据校验，不能假设客户端已经检查过。

### 参数为什么放在不同位置

| 位置 | 本实验示例 | 用途 |
|---|---|---|
| 路径 | `/api/notes/3` | 选择某个资源 |
| 查询参数 | `/api/notes?limit=2` | 筛选、分页或控制查询 |
| 正文 | `{"title":"HTTP"}` | 提交要创建的数据 |
| 请求头 | `Content-Type: application/json` | 描述消息格式等元数据 |

这些是接口设计惯例，需要由服务明确实现。不能把 title 写到 query 后，假定服务会自动与 JSON 合并；也不能用路径中的 ID 证明自己有权访问它。权限会在登录阶段加入。

### JSON 是交换格式，不是 Java 对象

```json
{"title":"HTTP","done":false,"count":2}
```

JSON 有对象、数组、字符串、数字、布尔值与 null；它没有 Java 方法、Date 对象或 undefined。属性名必须使用双引号，不能写注释或末尾多余逗号。

服务通常先把正文解析为可处理的数据，再做规则校验：

```text
原始字节 → 检查媒体类型 → JSON 解析 → 字段/业务规则检查 → 执行业务 → 响应
```

因此“不完整 JSON”和“完整 JSON 中 title 为空”发生在不同阶段。本实验分别返回 400 和 422。其他项目可能采用不同状态码，但仍应区分错误原因。

## GET 与 POST：行为比名称更重要

GET 的语义是读取资源，应当是安全的：客户端没有请求服务产生业务修改。服务仍可能写访问日志，这不等同于用户要求创建业务数据。

POST 常用于让目标资源处理提交内容，本实验用于创建条目。不要把“GET 参数只能在 URL、POST 参数只能在正文”当作协议的全部规则；具体输入方式以契约为准。本实验明确采用 GET query 与 POST JSON。

### 幂等性不等于每次返回相同内容

幂等描述重复相同请求的预期服务端效果与执行一次相同。GET 可以在两次请求之间读到别人更新的数据，仍符合幂等语义。

本实验重复发送两次相同 POST，会创建两个不同 ID 的条目。如果客户端因超时而重试，第一次其实可能已经创建成功。真实业务往往需要幂等键等设计，才能控制重复创建。超时只是“客户端没及时获得结果”，不能直接推断“服务端没执行”。

## origin、同源与 CORS 的基本边界

origin 由协议、主机和端口组成：

```text
http://127.0.0.1:8082
```

这个地址与 `http://127.0.0.1:5173` 端口不同，属于不同 origin；与 `http://localhost:8082` 主机名不同，也不算同源。

本实验的页面和 API 都由 8082 服务提供，`fetch('/api/notes')` 请求相同 origin。静态知识网站只展示教学内容，HTTP 实验应打开服务自带的本地页面。

浏览器的跨源访问限制与服务是否收到请求是两件事。某些跨源请求会先预检，某些请求会发出但响应不允许前端读取。curl 通常不执行浏览器的 CORS 规则；因此 curl 成功也不能证明跨源浏览器调用一定成功。后面会系统学习 CORS 与认证，这里先分清边界。

## 状态码能说明什么，不能说明什么

| 类别 | 基本含义 | 本实验关联 |
|---|---|---|
| 2xx | 请求成功处理 | 200 查询、201 创建 |
| 3xx | 重定向或缓存等语义 | 本实验不实现该流程 |
| 4xx | 请求侧条件导致无法按要求处理 | 400、404、405、413、415、422 |
| 5xx | 服务端无法完成请求 | 模拟 500 |

404 不一定说明“后端没启动”；已经拿到 HTTP 404，说明某个 HTTP 服务返回了响应。连接被拒绝通常没有 HTTP 状态码。500 也不等于整个服务器退出，它可能只影响某次请求。

单个状态码还不能告诉你完整原因。404 可能是路径拼错、资源不存在，或请求到了错误服务；应结合完整 URL、响应正文、契约和日志判断。

### 一个可靠的前端处理骨架

在本地实验页的 DevTools Console 中，可以用同源路径尝试：

```javascript
async function loadNotes() {
  try {
    const response = await fetch('/api/notes?limit=2');
    const body = await response.json();
    if (!response.ok) {
      console.error('HTTP 失败', response.status, body);
      return;
    }
    console.log('查询成功', body.items);
  } catch (error) {
    console.error('请求未完成或响应解析失败', error);
  }
}
loadNotes();
```

收到 404/500 时，fetch 通常会返回 Response，需要检查 `ok`。而 `response.json()` 本身也可能因正文不是有效 JSON 而抛异常，所以 catch 不全是网络错误。这个骨架针对本实验固定 JSON 响应；真实客户端还应处理取消、超时、不同正文类型和错误展示。

## 启动本地实验服务

**从 Mac 上的仓库根目录开始。** 工作区干净时先同步：

```bash
git pull --ff-only origin main
java -version
mvn -version
curl --version
cd projects/http-playground
mvn -B -ntp test dependency:copy-dependencies
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground
```

Maven 会运行已有的接口测试，并把运行依赖复制到 target/dependency。你本课需要会执行和观察结果，JUnit 的编写会在后续学习。

macOS classpath 使用 `:` 分隔；这里用引号包裹，避免 shell 提前展开 `*`。JVM 会使用指定目录的类和依赖。

预期终端提示端口 8082。**保持这个终端运行服务**；用另一个终端执行后面的 curl。结束时在服务终端按 Ctrl+C。

如果 8082 被其他程序占用，不要随意结束陌生进程。可改用：

```bash
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground 8083
```

之后把本课请求的端口一起改成 8083。


实验服务只绑定 127.0.0.1，没有登录和数据库，供本地学习使用。它不是可直接公开部署的完整后端。

### 当前接口契约

| 方法 | 路径 | 输入 | 正常输出 |
|---|---|---|---|
| GET | /api/notes | limit，可选，1–20，默认 10 | 200，items 与 total |
| GET | /api/notes/{id} | 路径中的 ID | 200，id 与 title |
| POST | /api/notes | JSON 的 title，非空字符串，最长 120 字符 | 201，新条目与 Location |
| GET | /api/demo/failure | 无 | 500，课程用的模拟失败 |

每次启动预置一条 ID 为 1 的知识条目。GET 列表中的 total 是内存中全部条目数，items 是本次按 limit 截取的结果。


## 浏览器实验：观察真正发送的内容

在你 **Mac 的本地浏览器地址栏**输入 `http://127.0.0.1:8082`。这需要本地服务已运行；云环境内的回环地址不会自动指向你的 Mac。

下面以 Chrome 为例：按 Command+Option+I 打开开发者工具，选择 Network，筛选 Fetch/XHR。

### A. GET 列表

1. 点击“准备 GET 列表”，再点击“发起请求”。
2. 在 Network 中选择 notes 请求。
3. 查看 Request URL、Request Method、Status Code。
4. 查看 Query String Parameters 与 Response。
5. 找出响应 Content-Type。

回答：limit 位于 URL 的哪一部分？这次 GET 是否发送了 JSON 正文？items 和 total 为什么可能不同？

### B. POST 创建

1. 点击“准备 POST 创建”。
2. 保持 Content-Type 为 application/json，正文为 `{"title":"理解 HTTP 请求"}`。
3. 发起请求，检查 Payload 中实际发送的正文。
4. 查看 201、Location 和响应中的 ID。
5. 再 GET 列表，确认新条目确实存在。

请求正文和响应正文是两个不同的数据：前者说明你要创建什么，后者告诉你实际创建了什么。

### C. 浏览器不会替后端完成校验

把 POST 正文改成 `{"title":"   "}`，发送后观察 422。即使按钮可以点击，服务端也应检查输入。

这个页面与 API 来自同一 origin，本课不会通过配置 CORS 解决其他错误。Cookie、Session、CORS、CSRF 在后续对应课程学习。


## curl 实验：离开浏览器重现同一个请求

打开第二个终端。HTTP 请求不依赖当前目录，保持服务终端运行即可。

### GET 列表

```bash
curl -i 'http://127.0.0.1:8082/api/notes?limit=2'
```

`-i` 将响应头与正文一起打印。带查询的 URL 用引号包裹，避免 zsh 把某些字符当作匹配模式。

### POST 创建

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  --data '{"title":"用 curl 创建的知识条目"}'
```

- `-X POST`：指定方法。
- `-H`：添加请求头。
- `--data`：发送正文；这份数据由单引号包裹，JSON 属性和值使用双引号。
- `\`：在 shell 中续行，它后面不要再添加空格。

curl 在使用 --data 时默认采用 POST，这里显式写出方法便于你观察。

### 根据 Location 读取

假设返回 Location 为 `/api/notes/3`，执行：

```bash
curl -i 'http://127.0.0.1:8082/api/notes/3'
```

以自己实际返回的 Location 为准。这个 GET 应返回刚才创建的条目。

### 有选择地观察请求

需要同时看请求与响应头时：

```bash
curl -v 'http://127.0.0.1:8082/api/notes?limit=1'
```

`>` 通常表示发送的头，`<` 表示收到的头。真实项目中，verbose 输出可能包含认证信息；分享前应删除这些字段。本实验不需要任何凭据。

注意小写 `-i` 与大写 `-I`：后者会发送 HEAD。本实验不支持 HEAD，因此不能用它替代 GET 验证。


## 失败实验：先判断哪个环节出了问题

### 400：JSON 语法错误

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":'
```

正文无法解析成完整 JSON，返回 400 和 invalid_json。错误请求不应创建条目。

### 415：正文类型与接口约定不符

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: text/plain' \
  --data '{"title":"HTTP"}'
```

即使内容看起来像 JSON，声明的媒体类型也不符合接口要求，返回 415。

### 422：JSON 可解析，但字段不符合规则

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":"   "}'
```

本实验用 422 表示 title 的业务输入规则未通过。有些接口统一使用 400，这要看契约；前后端需要对同一套规则达成一致。

### 404：指定的条目或接口不存在

```bash
curl -i 'http://127.0.0.1:8082/api/notes/999999'
```

服务仍在运行，已经收到并处理请求，然后明确告诉你资源不存在。

### 500：收到服务端失败响应

```bash
curl -i 'http://127.0.0.1:8082/api/demo/failure'
```

这是有意提供的失败模拟接口。随后请求列表仍应返回 200，说明收到 500 与“整个服务进程已经退出”不是同一件事。

### 扩展观察

- `GET /api/notes?limit=0` 返回 400：查询参数不符合契约。
- `DELETE /api/notes` 返回 405，响应头 Allow 表示目前支持的方法。
- 超过 8192 字节的 POST 正文返回 413。本课不必手工构造大正文。
- 401/403 会在登录权限阶段实现，本实验没有认证流程。


## HTTP 状态、工具退出码、前端异常各有含义

先执行：

```bash
curl -i 'http://127.0.0.1:8082/api/notes/999999'
echo $?
```

默认 curl 成功收到一个 404 响应，通常仍返回退出码 0。它完成了传输，但 HTTP 业务请求没有成功。

再执行：

```bash
curl --fail -sS 'http://127.0.0.1:8082/api/notes/999999'
echo $?
```

这次 --fail 将 HTTP 失败映射为 curl 的非零退出码，通常是 22，并不展示错误正文。想看接口错误内容时使用前面不带 --fail 的实验命令。

页面中的 fetch 也需要检查：

```javascript
const response = await fetch('/api/notes/999999');
if (!response.ok) {
  // 已收到 HTTP 响应；根据 status 和错误正文处理。
}
```

404/500 本身通常不会让 fetch reject。连接失败等问题才会进入相应异常路径。本课只用同源接口观察这一行为。

最后在服务终端按 Ctrl+C，重新执行 GET：现在是连接失败，通常没有 HTTP 状态码。再启动服务，确认它恢复初始条目。


## 怎么定位一次失败

按这个顺序收集证据：

1. 服务启动了吗？端口和地址是否正确？
2. 方法与路径是否符合接口契约？
3. 查询参数和正文是否放在正确位置？
4. Content-Type 与正文格式是否一致？
5. 状态码和错误正文是什么？
6. 是否需要进一步看服务日志或校验规则？

能用 curl 重现，有助于检查请求本身及后端处理。curl 和浏览器结果不同，还要比较两者实际发送的头、参数、正文及浏览器特有行为；不要只凭工具名称断定是谁的错误。


## 综合练习

先独立预测并执行请求，再展开答案核对。

### A. 解剖 GET（必做）

在浏览器 Network 与 curl 中请求 `/api/notes?limit=1`，检查：

1. 方法、完整 URL、path 与 query。
2. 状态码、响应 Content-Type。
3. items 的长度、total、二者是否一定相同。
4. GET 是否发送了本实验的 JSON 创建正文。

::: details 展开参考解释
方法为 GET，path 为 /api/notes，query 为 limit=1。正常返回 200，Content-Type 为 application/json; charset=utf-8。

新启动服务只有一条初始条目，此时 items 长度和 total 都是 1。创建更多条目后，limit=1 只返回一条，total 仍是全部条目数量。本实验的 GET 不需要 JSON 正文。
:::

### B. 创建后验证（必做）

用 curl 创建 title 为“我的 HTTP 观察记录”的条目，再依据实际 Location 读取。

先判断：POST 正文是否需要自己提供 id？为什么要再 GET 而不只看 201？重复发送同一 POST 会发生什么？

服务未实现重复提交保护。这就是后续学习幂等性时要解决的问题。

::: details 展开参考解释
客户端提供 title，服务生成 id。成功创建返回 201，并通过 Location 给出读取地址。再 GET 可确认资源能通过该接口实际读取，并核对标题与 ID。

相同 POST 重复提交会创建不同 ID 的条目；本实验没有幂等保护。不能把网络重试天然当作“只执行一次”，后续可靠性课程会进一步处理。
:::

### C. 区分错误层次（必做）

保持其他条件一致，分别发送：

| 输入/操作 | 先预测状态码 | 实际状态码与错误码 |
|---|---|---|
| application/json + 不完整正文 `{"title":` | | |
| text/plain + 完整 JSON | | |
| application/json + `{"title":"   "}` | | |
| GET 不存在的条目 | | |
| GET /api/demo/failure | | |

对每一行解释：请求是否到达服务？是否成功解析？是否符合字段规则？仅凭状态码能否断定全部根因？

::: details 展开参考解释
| 场景 | 状态码 | 本服务错误码 | 原因 |
|---|---:|---|---|
| 不完整 JSON | 400 | invalid_json | 无法解析正文 |
| text/plain | 415 | unsupported_media_type | 媒体类型不符 |
| 空白 title | 422 | invalid_title | JSON 正确但字段规则未通过 |
| 不存在的 ID | 404 | note_not_found | 没有对应资源 |
| 失败模拟 | 500 | demo_failure | 专门模拟服务端失败 |

这些状态码都是服务收到请求后的响应。具体原因还需要看正文、接口规则与日志。当前验证顺序先检查媒体类型，所以同时使用 text/plain 和损坏正文时优先得到 415；不要忽略其他输入条件。
:::

### D. 三种“失败”（必做）

1. curl 收到 404 时，立即检查 `$?`。
2. 带 --fail 再请求，立即检查 `$?`。
3. 在页面中请求不存在的条目，观察 response.ok 与页面状态。
4. 停止服务，再请求同一个地址，比较有没有 HTTP 响应。

解释传输结果、HTTP 状态、JavaScript Promise 三者的关系。

::: details 展开参考解释
默认 curl 已经完成一次 HTTP 交换，即使服务返回 404，也通常以 0 退出。--fail 将该 HTTP 失败映射为非零退出码，本实验是 22。

fetch 收到 404 时通常仍得到 Response 对象，response.ok 为 false；需要程序主动检查状态。连接失败时才会走相应异常路径，通常没有服务器返回的 HTTP 状态码。网络、浏览器和实际客户端行为还可能影响异常分类，不应只按一个数字猜原因。
:::

### E. 把输入放错位置（扩展）

向 `/api/notes?title=HTTP` 发 POST，Content-Type 为 application/json，正文是 `{}`。

预测标题是否会创建成功。对照接口契约，解释服务为什么不会自动把 query 的 title 当成正文字段。

::: details 展开参考解释
返回 422。本服务约定从 JSON 正文读取 title，不会自动把 URL query 中的同名字段合并到正文。输入的位置是契约的一部分。
:::

### F. 方法与缓存（扩展）

1. 用 DELETE 请求 `/api/notes`，查看状态码和 Allow。
2. 比较 curl -i 与 curl -I，说明为什么不能随意替换。
3. 查看本服务 Cache-Control，说明为什么重复 GET 不会观察到本课未实现的 304 流程。

本课不要求修改服务实现。能用具体请求解释结果，就是本次练习的重点。

::: details 展开参考解释
DELETE 返回 405，Allow 为 GET, POST。curl -i 仍使用当前请求方法并显示响应头，curl -I 会改为 HEAD；本服务不支持 HEAD，所以是 405 且没有响应正文。

Cache-Control 为 no-store，浏览器不应存储本响应用于缓存复用。本服务没有实现条件请求与 304，后续可以用独立实验再学习缓存。


:::

## 延伸阅读

- [MDN：HTTP 消息](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Messages)：对照请求、响应结构。
- [MDN：HTTP 方法](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods)：本课重点 GET、POST。
- [MDN：HTTP 状态码](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status)：查本课遇到的状态。
- [Chrome Network 面板文档](https://developer.chrome.com/docs/devtools/network/)：查看 Headers、Payload、Response。
- [curl 官方手册](https://curl.se/docs/manpage.html)：查 -i、-v、-H、--data、--fail。
- [B 站视频检索：HTTP 请求响应、curl](https://search.bilibili.com/all?keyword=HTTP%20%E8%AF%B7%E6%B1%82%E5%93%8D%E5%BA%94%20curl)：检索入口，未指定单个视频；有需要时只补相应主题。

本课程可独立完成，资料用于核对与补充。没有必要先看完一整套网络课程。


## 下一层知识

[Java 输入、控制流与方法](/lessons/java-control-flow) 将亲手实现与后端处理相同的基本过程：读取输入、解析、校验，再决定返回什么结果。
