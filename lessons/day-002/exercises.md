# Day 002：HTTP 观察与诊断练习

服务运行后开始。每题先写预期，再运行；具体结果写进 [观察模板](worksheet.md)，别用参考答案代替实际结果。

## A. 解剖 GET（必做）

在浏览器 Network 与 curl 中请求 `/api/notes?limit=1`，记录：

1. 方法、完整 URL、path 与 query。
2. 状态码、响应 Content-Type。
3. items 的长度、total、二者是否一定相同。
4. GET 是否发送了本实验的 JSON 创建正文。

## B. 创建后验证（必做）

用 curl 创建 title 为“我的 HTTP 观察记录”的条目，再依据实际 Location 读取。

先判断：POST 正文是否需要自己提供 id？为什么要再 GET 而不只看 201？重复发送同一 POST 会发生什么？

服务未实现重复提交保护。把结果作为后续幂等学习的问题记录下来。

## C. 区分错误层次（必做）

保持其他条件一致，分别发送：

| 输入/操作 | 先预测状态码 | 实际状态码与错误码 |
|---|---|---|
| application/json + 不完整正文 `{"title":` | | |
| text/plain + 完整 JSON | | |
| application/json + `{"title":"   "}` | | |
| GET 不存在的条目 | | |
| GET /api/demo/failure | | |

对每一行解释：请求是否到达服务？是否成功解析？是否符合字段规则？仅凭状态码能否断定全部根因？

## D. 三种“失败”（必做）

1. curl 收到 404 时，立即检查 `$?`。
2. 带 --fail 再请求，立即检查 `$?`。
3. 在页面中请求不存在的条目，观察 response.ok 与页面状态。
4. 停止服务，再请求同一个地址，比较有没有 HTTP 响应。

解释传输结果、HTTP 状态、JavaScript Promise 三者的关系。

## E. 把输入放错位置（扩展）

向 `/api/notes?title=HTTP` 发 POST，Content-Type 为 application/json，正文是 `{}`。

预测标题是否会创建成功。对照接口契约，解释服务为什么不会自动把 query 的 title 当成正文字段。

## F. 方法与缓存（扩展）

1. 用 DELETE 请求 `/api/notes`，查看状态码和 Allow。
2. 比较 curl -i 与 curl -I，说明为什么不能随意替换。
3. 查看本服务 Cache-Control，说明为什么重复 GET 不会观察到本课未实现的 304 流程。

今天不要求修改服务实现。能用具体请求解释结果，就是本次练习的重点。
