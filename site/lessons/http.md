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

从 Mac 的仓库根目录开始，执行：

```bash
java -version
mvn -version
cd projects/http-playground
mvn -B -ntp test dependency:copy-dependencies
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground
```

如果找不到 Java 或 Maven，先回到 [macOS 环境准备](/setup)。Maven 命令会运行接口测试，并准备服务需要的依赖；最后一行才是真正启动服务。

看到启动提示后，**保留这个终端**。程序要一直运行，才能继续接收请求。另开一个终端，用来执行下面的 `curl` 命令。

在 Mac 浏览器中打开 `http://127.0.0.1:8082/`，你会看到实验页面。`127.0.0.1` 指当前电脑，所以服务和浏览器应运行在同一台 Mac 上。

::: details 端口被占用，或者想知道启动命令的含义？
若提示地址已被占用，可以让同一个程序使用另一个端口：

```bash
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground 8083
```

后面所有地址的端口也一起改成 8083，不必结束不认识的进程。

`-cp` 指定在哪里找类。`target/classes` 放本项目编译后的类，`target/dependency/*` 放第三方依赖。macOS 用 `:` 分隔这些位置；引号防止 shell 提前展开星号。第 01 课的编译、classpath 和完整类名，在这里仍然适用。

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
- [Chrome Network 面板](https://developer.chrome.com/docs/devtools/network/)：对照 Headers、Payload、Response 操作。
- [curl 手册](https://curl.se/docs/manpage.html)：按需查 `-i`、`-v`、`-H`、`--data`、`--fail`。

本课讲解参考 MDN 的请求与响应模型，例子使用仓库里的可运行服务。先完成查询、创建和三种输入错误，再读补充资料，理解会更具体。

下一课 [Java 输入、控制流与方法](/lessons/java-control-flow)，亲手写出“接收输入 → 转换 → 判断 → 输出”的程序，理解后端处理数据所需的基础。
