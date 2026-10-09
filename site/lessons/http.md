---
description: 从查询和创建一条学习笔记开始，读懂 HTTP 请求与响应，再用具体错误学会诊断接口。
---

# 02 · HTTP 请求、响应与诊断

你做前端时大概写过这样的代码：

```javascript
const response = await fetch('/api/notes');
const data = await response.json();
```

这两行并没有直接访问某个 Java 变量。浏览器先向一个地址发送请求，运行在那个地址的程序处理请求，再把结果传回来。

这一课围绕一个小功能展开：**查询学习笔记，再创建一条新笔记**。你会亲眼看到前端到底发了什么、后端到底回了什么。暂时不用读懂服务的全部源码，把它当作可以操作的实验工具。

## 启动本地实验服务

从仓库根目录开始，执行：

```bash
java -version
mvn -version
cd projects/http-playground
mvn -B -ntp test dependency:copy-dependencies
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground
```

本课使用 Java 21 和 Maven。Maven 命令会运行接口测试，并准备服务需要的依赖；最后一行才是真正启动服务。

看到启动提示后，**保留这个终端**。程序要一直运行，才能继续接收请求。另开一个终端，用来执行下面的 `curl` 命令。

在浏览器中打开 `http://127.0.0.1:8082/`，你会看到实验页面。`127.0.0.1` 指当前电脑，所以服务和浏览器应运行在同一台电脑上。

::: details 端口被占用，或者想知道启动命令的含义？
若提示地址已被占用，可以让同一个程序使用另一个端口：

```bash
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground 8083
```

后面所有地址的端口也一起改成 8083，不必结束不认识的进程。

`-cp` 指定在哪里找类。`target/classes` 放本项目编译后的类，`target/dependency/*` 放第三方依赖。这里用 `:` 分隔位置，引号防止 shell 提前展开星号。第 01 课的编译、classpath 和完整类名，在这里仍然适用。

实验服务只监听本机，数据只存在内存，没有登录功能。后续会逐步补上数据库和认证。
:::

## 先查一次笔记，看清返回结果

在第二个终端执行：

```bash
curl -i 'http://127.0.0.1:8082/api/notes?limit=2'
```

`curl` 是一个能发送 HTTP 请求的命令行工具；`-i` 让它把响应头和正文一起显示。URL 加引号，避免终端把其中的字符当作特殊语法。

刚启动服务时，你会看到类似结果。这里省略了时间等响应头：

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8
Cache-Control: no-store

{"items":[{"id":1,"title":"第1天：Java工具链"}],"total":1}
```

先读三件事：

- `200`：这次查询成功。
- `Content-Type`：正文是 JSON，文本使用 UTF-8 编码。
- 空行后面的内容：真正的查询结果，有一个笔记数组 `items` 和总数 `total`。

`limit=2` 表示本次最多返回两条，**不会凭空创造第二条笔记**。目前总共只有一条，所以数组长度是 1。以后总共五条时，`limit=2` 得到的数组长度是 2，但 `total` 仍然是 5。

现在回到实验页面，打开 Chrome 开发者工具（Command+Option+I），选择 Network，筛选 Fetch/XHR。点击“准备 GET 列表”，再点击“发起请求”，选中 notes 请求。Headers 里能看到方法、地址和状态，Response 里能看到同一份 JSON。

浏览器和 curl 都能向服务发送请求。Network 面板展示的是实际交换的数据，比只看前端代码更有助于发现问题。

## 一个 URL，分别告诉服务哪些信息

拆开刚才的地址：

```text
http://127.0.0.1:8082/api/notes?limit=2
```

| 部分 | 本例 | 可以怎样理解 |
|---|---|---|
| 协议方案 | `http` | 本次用 HTTP 进行交换 |
| 主机 | `127.0.0.1` | 找当前电脑 |
| 端口 | `8082` | 找这台电脑上监听这个端口的服务 |
| 路径 | `/api/notes` | 找笔记相关的接口 |
| 查询参数 | `limit=2` | 把查询条件交给接口 |

你可以把主机和端口理解为“找到服务”，把路径和方法理解为“告诉服务要做什么”。这是帮助入门的理解方式，之后还会展开连接建立的过程。

如果 URL 末尾有 `#results`，这是片段标识，通常由浏览器使用，不会作为 HTTP 请求目标发送给服务。

本课使用本机 HTTP。远程接口常用 HTTPS，增加 TLS 对传输的保护；下一层网络知识再解释 DNS、连接和 TLS。

## 再创建一条笔记，看懂请求的组成

执行：

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":"理解 HTTP 请求"}'
```

这条命令在说：“请在笔记集合里，创建标题为‘理解 HTTP 请求’的笔记。”

| 命令部分 | 实际作用 |
|---|---|
| `-X POST` | 指定操作方法为 POST |
| URL | 指定请求发给哪个接口 |
| `-H` | 添加请求头，说明正文格式 |
| `--data` | 提供要发送的正文 |
| 行末的 `\` | 告诉 shell 命令下一行还没结束；后面不能再加空格 |

外面的单引号属于 shell，里面的双引号属于 JSON。设置 `Content-Type` 并不会自动把任意文字变成 JSON，正文仍要写对。

新启动实例的第一次创建，关键响应类似：

```http
HTTP/1.1 201 Created
Content-Type: application/json; charset=utf-8
Location: /api/notes/2

{"id":2,"title":"理解 HTTP 请求"}
```

请求只提供标题；ID 由服务生成。`201` 表示创建成功，`Location` 给出新笔记的位置。**以自己的响应为准**，如果已经创建过笔记，ID 就可能不是 2。

用实际 ID 再查一次：

```bash
curl -i 'http://127.0.0.1:8082/api/notes/2'
```

这一步验证了：刚才创建的笔记，确实能够通过查询接口读到。在实验页面也可以准备 POST、修改正文、发起请求，再在 Network 的 Payload 和 Response 中对照输入与输出。

### 请求和响应，不是同一份数据

到这里再归纳，就容易理解了：

```text
请求：方法 + 地址 + 请求头 + 请求正文
       POST   /api/notes   格式说明   我要创建的标题
                          ↓
                  服务解析、校验、保存
                          ↓
响应：状态码 + 响应头 + 响应正文
       201    格式/新资源位置   实际生成的 ID 和标题
```

GET 表示读取资源，POST 在本接口中表示创建资源。同样的路径，配不同方法，可以执行不同工作。GET 查询不会因为多调用一次就创建新笔记。

::: details HTTP 消息、Content-Type 和 Accept 的更多细节
下面是创建请求的 HTTP/1.1 文本示意，省略了正文长度等头：

```http
POST /api/notes HTTP/1.1
Host: 127.0.0.1:8082
Content-Type: application/json
Accept: application/json

{"title":"HTTP"}
```

空行把头和正文隔开。工具会计算并发送所需的消息长度信息；正文长度按字节计算，不能把中文字符数直接当作字节数。

请求 `Content-Type` 表示“我发送什么格式”；请求 `Accept` 表示“我希望收到什么格式”；响应 `Content-Type` 表示“服务实际返回什么格式”。本服务的 API 固定返回 JSON。

HTTP 头名不区分大小写。HTTP/2、HTTP/3 的线上编码方式与这段文本不同，但方法、头、状态码等概念仍适用。
:::

## 接口契约：为什么不能想怎么传就怎么传

前后端需要共同约定输入输出规则，这份约定叫**接口契约**。本实验的主要接口是：

| 方法与路径 | 输入规则 | 返回结果 |
|---|---|---|
| `GET /api/notes` | 可选 query `limit`，1–20，默认 10 | 200：`items`、`total` |
| `GET /api/notes/{id}` | 路径中提供笔记 ID | 200：笔记；找不到则 404 |
| `POST /api/notes` | JSON 对象，`title` 为字符串，去首尾空白后非空，最多 120 个 Unicode 码点 | 201：新笔记；`Location` 指向它 |
| `GET /api/demo/failure` | 无 | 500：教学用模拟失败 |

例如把标题放进 URL：

```text
POST /api/notes?title=HTTP
正文：{}
```

它不会创建成功，因为当前接口约定从 **JSON 正文**读取标题。服务不会自动把不同位置的同名字段合并。

前端 `required` 只是帮助用户输入。别人能绕过页面直接发请求，所以后端也必须检查输入。

### JSON 写对了，数据也可能不合格

把下面三份正文分开看：

| 正文 | 能解析成 JSON 吗？ | 满足创建规则吗？ |
|---|---|---|
| `{"title":` | 不能，不完整 | 还没到规则检查 |
| `{"title":"   "}` | 能 | 不能，标题只有空白 |
| `{"title":123}` | 能 | 不能，标题应为字符串 |

JSON 是文本交换格式，不是 Java 或 JavaScript 对象本身。属性名要加双引号，不支持注释、尾随逗号或 `undefined`。服务先解析这段文本，再检查它是否符合接口要求。

::: details 为什么长度规则写 Unicode 码点？
用户眼中的一个“字符”、Unicode 码点、UTF-16 单元和 UTF-8 字节并不总是一一对应。本服务标题上限按 Unicode 码点计算，正文大小上限按字节计算。当前先知道“长度必须明确怎么算”，处理 emoji 和复杂文本时再深入。
:::

## 从一个功能，扩展到一组接口

如果之后要支持“修改、删除笔记”，你会怎样命名接口？可以先按资源组织，再选择方法：

| 需求 | 常见设计 | 重复执行的预期效果 |
|---|---|---|
| 查询列表 | `GET /api/notes` | 读取，不要求改变业务数据 |
| 查询一条 | `GET /api/notes/2` | 读取同一个目标 |
| 创建一条 | `POST /api/notes` | 可能再次创建 |
| 完整替换 | `PUT /api/notes/2` | 用同一份内容替换，目标最终状态相同 |
| 局部修改 | `PATCH /api/notes/2` | 取决于修改内容和实现 |
| 删除一条 | `DELETE /api/notes/2` | 目标最终都应不存在 |

**后面三种是未来接口的设计示例，当前实验服务没有实现。** 直接调用会得到 405，不能把表格当作已存在的功能。

路径通常使用资源名，避免把每个接口都写成 `/doCreateNote`、`/doGetNote`。这类风格有助于统一表达，但仍要配合准确契约；用了名词并不等于整个系统自动具备良好设计。

### 安全与幂等，分别在描述什么

GET 被称为安全方法，是指它的语义没有要求产生业务修改；记录访问日志不因此变成“用户请求修改笔记”。

幂等看的是**重复请求对服务端的预期效果**，不是每次响应内容必须相同。例如第一次 DELETE 成功，第二次可能返回 404，但目标仍然不存在，仍可符合幂等语义。

PUT “把标题设为 Java”与 PATCH “把阅读次数加一”也不同。前者重复写仍是 Java；后者若每次都加一，就不幂等。不能只记 PATCH 这个名字就决定安全重试。

POST 把参数放正文，并不会让它天然比 GET 安全；HTTPS 保护传输，认证和授权控制访问。也不能把敏感信息放进 URL 后，仅因为“浏览器有 HTTPS”就忽略日志与历史中的暴露。

## 查询参数：先正确编码，再谈分页

前端不要靠手拼字符串处理所有查询。这个片段只演示构造 URL，不会请求尚未实现的搜索接口：

```javascript
const params = new URLSearchParams({
  keyword: 'Java & HTTP',
  limit: '2'
});
const url = `/api/notes?${params.toString()}`;
console.log(url);
```

`&` 在 URL query 中原本是参数分隔符；编码后才能成为 keyword 的内容。空格、中文和其他特殊字符也应按规则编码。服务收到后再解码；不要重复编码或把整段 URL 全部当作一个参数编码。

当前服务只实现 limit。想验证参数约定，可以运行：

```bash
curl -i 'http://127.0.0.1:8082/api/notes?limit=2'
curl -i 'http://127.0.0.1:8082/api/notes?limit=2&limit=3'
```

第二条返回 400：本服务明确不接受重复的 limit。不同服务处理重复参数的方式可能不同，不能假设取第一个、最后一个或自动合并。

### limit 不等于完整分页

当前 limit 只能截取前几条，不能翻到下一页。完整分页还需要规定起点或游标，以及稳定排序。

例如 offset 分页可以约定“按 ID 排序，跳过前 20 条，再取 10 条”；游标分页可以约定“从上一批最后的 ID 之后继续”。这些是设计说明，当前 API 不支持对应参数。

如果没有稳定排序，两次请求之间可能漏读或重复读。数据库阶段会把分页 SQL、索引和数据变化结合起来讲；现在先知道 items、total、limit 各自回答什么问题。

## 主动制造错误，比背状态码更容易记住

保持服务运行，分别发送下面的请求。你会看到“失败”发生在不同地方。

### 格式声明不对：415

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: text/plain' \
  --data '{"title":"HTTP"}'
```

正文看起来像 JSON，但声明成了普通文本。接口不接受这种格式，所以返回 `415`，错误码为 `unsupported_media_type`。

### JSON 文本不完整：400

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":'
```

类型声明通过了，但解析不了正文，返回 `400` 和 `invalid_json`。

### JSON 没问题，标题不合格：422

```bash
curl -i -X POST 'http://127.0.0.1:8082/api/notes' \
  -H 'Content-Type: application/json' \
  --data '{"title":"   "}'
```

正文能解析，但标题不符合规则，返回 `422` 和 `invalid_title`。

这三种请求都不应该创建笔记。错误响应也有正文，例如：

```json
{"error":{"code":"invalid_title","message":"title 须为非空字符串，最长 120 个字符"}}
```

`message` 的具体措辞以实际响应为准，前端应优先依据稳定的 `code` 区分错误。其他项目可能把字段错误统一归为 400；要看契约，不能把本实验的所有数字当成唯一标准。

### 资源不存在：404；服务主动模拟失败：500

```bash
curl -i 'http://127.0.0.1:8082/api/notes/999999'
curl -i 'http://127.0.0.1:8082/api/demo/failure'
curl -i 'http://127.0.0.1:8082/api/notes'
```

第一条返回 404，第二条返回 500，第三条仍应返回 200。

**收到 404，说明某个 HTTP 服务已经给了响应；收到 500，也不表示整个进程退出了。** 本例的 500 只是专门设置的模拟分支。

最后在服务终端按 Ctrl+C，再查列表。这时通常是连接失败，连 HTTP 响应都没有。它和 404 是两种不同现象。

::: details 其余状态码和请求观察工具
本服务还支持这些失败实验：

- `GET /api/notes?limit=0` → 400：查询条件不合格。
- `DELETE /api/notes` → 405：不支持这个方法；响应头 `Allow` 说明允许的方法。
- POST 正文超过 8192 字节 → 413：正文过大。

通常 2xx 表示成功，3xx 涉及重定向等语义，4xx 表示请求条件不满足，5xx 表示服务端失败。401、403 会在身份与权限课程里实现。

需要观察发送和收到的头，可以使用 `curl -v`。小写 `-i` 是显示响应头，大写 `-I` 会改发 HEAD；本服务不支持 HEAD，所以不要混用。

默认 curl 收到 404 仍通常以退出码 0 结束，因为传输完成了。加 `--fail` 会把此类 HTTP 失败映射成非零退出码，404 时通常为 22。可以在命令结束后单独执行 `echo $?` 查看。工具退出码和 HTTP 状态码不是同一个概念。
:::

## JSON、文本与空正文：别把所有响应都当成同一种

本服务 API 固定返回 JSON，但网站首页返回 HTML。请求成功后立即 `response.json()`，遇到 HTML 也会解析失败。

```text
HTTP 成功了吗？ → 看状态
正文是什么？   → 看 Content-Type 与契约
字段可靠吗？   → 看解析结果与结构
```

204 表示成功但没有正文，不应再要求它提供 JSON。301、302 等重定向可能由浏览器或 fetch 自动跟随，所以最终拿到的 URL、状态未必是最初地址的响应。当前实验不提供这两种流程，遇到真实接口时再按 Network 追踪。

### 前端类型声明不能代替运行时检查

在 TypeScript 中写 `const data: Note[] = ...`，不会把服务器响应自动校验成 Note 数组。类型声明会被编译掉，网络输入仍可能缺字段、类型不对或来自错误服务。

至少先确认列表响应是对象、items 是数组、total 是非负整数；需要严格契约时再引入运行时 schema 校验。后端和前端都要对边界输入保持明确约定。

## 为什么有时接口没发请求，也能显示旧内容

浏览器可能复用之前的响应，这叫 HTTP 缓存。它与页面组件中存了一份列表是不同机制，与数据库持久化也不是同一件事。

| 常见响应头 | 含义 |
|---|---|
| `Cache-Control: no-store` | 不存储这份响应用于缓存复用 |
| `Cache-Control: no-cache` | 可以存储，但复用前要向服务验证 |
| `Cache-Control: max-age=60` | 在符合缓存规则的情况下，新鲜期内可复用 |
| `ETag: "v1"` | 服务提供的表示版本标识，可用于验证 |

例如客户端缓存了 ETag 为 v1 的内容，之后带 `If-None-Match: "v1"` 查询。服务确认仍未变化时，可以返回 304，让客户端继续用原正文。这不是“错误返回了空数据”。

**当前服务设置 no-store，没有实现 ETag 或 304。** 可以用 curl 观察响应头确认这一点，不要在这个实验里寻找不存在的缓存命中。

静态资源、用户私有数据、实时任务状态应分别制定缓存策略。遇到“页面为什么还是旧的”，先区分组件状态、HTTP 缓存、服务数据，再决定查哪里。

## 回到前端：为什么 404 不会自动进入 catch

重新启动服务，在 **8082 实验页面**的 Console 中执行：

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
    console.error('请求未完成，或正文无法解析', error);
  }
}
loadNotes();
```

把地址改成 `/api/notes/999999` 再试。`fetch` 收到 404 时通常仍会得到一个 `Response`，只是 `response.ok` 为 false。只有你主动检查，才能按 HTTP 失败处理。

连接失败可能让 `fetch` 抛异常；`response.json()` 遇到非 JSON 正文也会抛异常。因此 catch 里的错误不全是网络错误。这个示例针对本服务固定 JSON 的约定，后续再加入取消、超时和完整错误展示。

### 相对路径究竟请求哪台服务

在 `http://127.0.0.1:8082/` 页面写 `fetch('/api/notes')`，目标是这个地址下的接口。但在 `http://localhost:5173/dailyStudy/` 知识网站写同一句，目标会是 `http://localhost:5173/api/notes`。

**相对路径继承当前页面的 origin。** origin 由协议、主机、端口组成，不会根据接口名称自动找到另一个服务。本课用服务自带页面，让页面和接口保持同源。

::: details 为什么 curl 成功，浏览器却说跨域？
浏览器对跨源请求有额外限制，这就是之后要学的 CORS。不同端口就是不同 origin；`localhost` 与 `127.0.0.1` 也不是相同主机名。

有些跨源请求先发预检，有些已经到达服务但响应不能被页面读取。curl 通常不执行浏览器的 CORS 检查，所以“curl 成功”只证明该具体请求能工作，不能直接证明浏览器跨源调用也会成功。
:::

## 再想一步：重复创建会怎样

连续发送两次相同的合法 POST，会生成两条不同 ID 的笔记。它们标题相同，仍是两次创建。

如果第一次创建已经成功，只是响应迟迟没到，前端因为超时又发一次，可能造成重复数据。**超时只代表客户端没有及时拿到结果，不能证明服务没有执行。**

这引出后续的“幂等性”：重复操作的预期服务端效果，是否与一次操作相同。GET 应用于读取，可以重复查询；本实验 POST 创建没有重复提交保护。以后会用幂等键等方案处理需要重试的写操作。

## CORS 预检：跨源 JSON POST 为什么可能发出两次请求

前面分清了 origin，现在继续看一个典型场景：页面在 5173，向 8082 发 JSON POST。这通常先触发 OPTIONS 预检：

```text
浏览器 → OPTIONS：这个 origin 能用 POST 和 Content-Type 吗？
服务   → 给出允许的 origin、方法和头
浏览器 → 条件满足，才发送真正的 POST
```

`application/json` 不属于 CORS 允许免预检的那几种 Content-Type。因此 GET 能到达接口，JSON POST 却只有 OPTIONS，可能是预检没通过。

当前服务没有配置跨源访问，所以仍在它自带的页面做同源实验。等前后端分离联调时，再配置明确的允许 origin、方法、头与凭据策略。

不要用 `mode: 'no-cors'` 当通用修复：它会限制可发送内容，并使响应对脚本不可读。也不要把 CORS 当成后端权限系统：curl 等客户端不受同一套浏览器检查限制，真正的资源权限必须由服务检查。

## 综合实践：写一个会检查结果的客户端函数

下面这段可在 **8082 实验页面的 Console** 中整体执行。它围绕当前 JSON API，增加状态处理、超时取消、列表结构检查；不是通用到所有协议与响应格式的完整 SDK。

```javascript
async function requestJson(path, options = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 5000);
  try {
    const response = await fetch(path, {
      ...options,
      signal: controller.signal
    });
    if (response.status === 204) return null;

    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.includes('application/json')) {
      throw new Error(`预期 JSON，实际为 ${contentType || '未声明格式'}`);
    }
    const body = await response.json();
    if (!response.ok) {
      const code = body?.error?.code ?? 'unknown_error';
      throw new Error(`HTTP ${response.status}: ${code}`);
    }
    return body;
  } finally {
    clearTimeout(timer);
  }
}

async function listNotes() {
  const body = await requestJson('/api/notes?limit=2');
  if (body === null || typeof body !== 'object' ||
      !Array.isArray(body.items) || !Number.isInteger(body.total) ||
      body.total < 0) {
    throw new Error('列表响应结构不符合约定');
  }
  return body;
}

async function createNote(title) {
  return requestJson('/api/notes', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title })
  });
}

async function demoClient() {
  try {
    const created = await createNote('用客户端函数创建');
    console.log('已创建', created);
    console.log('再查询', await listNotes());
  } catch (error) {
    console.error('本次操作失败', error);
  }
}
demoClient();
```

沿执行顺序解释：构造请求 → 限制等待 → 确认响应格式 → 解析正文 → 检查 HTTP 状态 → 检查业务结构。finally 无论成功或失败都清除定时器。

AbortController 让客户端停止等待或读取，不保证服务器已经停止执行。当前快速接口大多在五秒内返回；超时能力存在，不代表每次实验都会触发。helper 也没有替你实现后端幂等保护。

### 按正常、错误与结构三条路径验证

1. 运行 demoClient，看到创建结果与列表；重复运行应再次创建。
2. 将标题改为三个空格，确认显示 `HTTP 422: invalid_title`，而不是成功提示。
3. Console 执行 `requestJson('/')`：它收到 HTML，应提示预期 JSON，而不是假装得到合法列表。
4. 单独执行 `requestJson('/api/notes/999999')` 并用 `.catch(console.error)` 观察 404；对比它与连接失败的异常。

::: details 参考观察与可以继续改的地方
正常路径返回服务生成的 ID；空白标题在 HTTP 状态检查中失败；首页的 200 仍因正文类型不符合约定而失败；不存在的 ID 会显示 `HTTP 404: note_not_found`。

现在 listNotes 只检查外层结构。你可以继续逐项检查 `id` 为正整数、`title` 为字符串，并把显示错误与请求逻辑分开。真正用于项目时，还需要明确身份、取消来源、重试策略与错误分类。
:::

## 练习：先预测，再实测

### A. 查询数量

刚启动时查 `?limit=1`，再创建两条笔记后查同一个地址。两次的 `items` 长度和 `total` 是多少？

::: details 展开参考解释
刚启动时两者都是 1。创建两条后，总数为 3，但只返回一条，所以 `items.length=1`、`total=3`。limit 控制返回数量，不是数据总量。
:::

### B. 改错一份请求

你向 `/api/notes?title=HTTP` 发 POST，正文 `{}`，Content-Type 为 application/json。为什么创建不了？写出修正后的正文。

::: details 展开参考解释
本接口从正文读取 title，query 不会自动补进去；返回 422。正文改为 `{"title":"HTTP"}`，保留 POST 和 application/json。
:::

### C. 区分三个失败

对比 text/plain + 完整 JSON、application/json + 不完整 JSON、application/json + 数字 title。哪一步拒绝了请求？

::: details 展开参考解释
依次为类型检查 415、JSON 解析 400、字段校验 422。后面两者分别是“读不懂正文”和“读懂了但不符合规则”。
:::

### D. 调试前端

`fetch('/api/notes/999999')` 没进入 catch，所以同事说“查询成功了”。这句话错在哪里？

::: details 展开参考解释
Promise 没有 reject 不能代表 HTTP 成功。还要检查 `response.ok` 或状态码，并读错误正文。本例已经收到 404，查询并未成功。
:::

### E. 找错地址

网站在 5173，API 在 8082。网站里 `fetch('/api/notes')` 访问哪一个？你会在 Network 的哪个字段验证？

::: details 展开参考解释
访问网站所在服务，即 5173。查看 Request URL，不仅看前端代码里的相对路径。需要开发代理或明确的 API 地址才能改变目标，跨源调用还需考虑 CORS。
:::

### F. 请求失败后能否直接重试

创建请求超时了，直接再发一次一定安全吗？本实验会怎样？

::: details 展开参考解释
不一定。第一次可能已经写入，只是响应没被收到。本实验相同 POST 会再次创建，可能重复。后续要根据操作语义和幂等设计决定重试策略。
:::

## 资料怎么配合这一课使用

- [MDN：HTTP 概述](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Guides/Overview)：补充客户端、服务端、消息的整体关系。
- [MDN：使用 Fetch](https://developer.mozilla.org/zh-CN/docs/Web/API/Fetch_API/Using_Fetch)：重点看检查状态与读取正文。
- [MDN：HTTP 状态码](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status)：遇到哪一个查哪一个，不用先背全表。
- [MDN：HTTP 方法](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Reference/Methods)：对照 GET、POST、PUT、PATCH、DELETE 的语义。
- [MDN：HTTP 缓存](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Guides/Caching)：重点读 no-store、no-cache 与验证流程。
- [MDN：CORS](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Guides/CORS)：按 origin、预检、响应读取的顺序读。
- [Chrome Network 面板](https://developer.chrome.com/docs/devtools/network/)：对照 Headers、Payload、Response 操作。
- [curl 手册](https://curl.se/docs/manpage.html)：按需查 `-i`、`-v`、`-H`、`--data`、`--fail`。

本课讲解参考 MDN 的请求与响应模型，例子使用仓库里的可运行服务。先完成查询、创建和三种输入错误，再读补充资料，理解会更具体。

下一课 [Java 输入、控制流与方法](/lessons/java-control-flow)，亲手写出“接收输入 → 转换 → 判断 → 输出”的程序，理解后端处理数据所需的基础。
