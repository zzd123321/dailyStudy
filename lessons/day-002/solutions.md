# Day 002 参考答案

先做练习再查看。条目数量、ID 和响应时间取决于你实际操作，记录时使用实测值。

## A

方法为 GET，path 为 /api/notes，query 为 limit=1。正常返回 200，Content-Type 为 application/json; charset=utf-8。

新启动服务只有一条初始条目，此时 items 长度和 total 都是 1。创建更多条目后，limit=1 只返回一条，total 仍是全部条目数量。本实验的 GET 不需要 JSON 正文。

## B

客户端提供 title，服务生成 id。成功创建返回 201，并通过 Location 给出读取地址。再 GET 可确认资源能通过该接口实际读取，并核对标题与 ID。

相同 POST 重复提交会创建不同 ID 的条目；本实验没有幂等保护。不能把网络重试天然当作“只执行一次”，第 16 周会进一步处理。

## C

| 场景 | 状态码 | 本服务错误码 | 原因 |
|---|---:|---|---|
| 不完整 JSON | 400 | invalid_json | 无法解析正文 |
| text/plain | 415 | unsupported_media_type | 媒体类型不符 |
| 空白 title | 422 | invalid_title | JSON 正确但字段规则未通过 |
| 不存在的 ID | 404 | note_not_found | 没有对应资源 |
| 失败模拟 | 500 | demo_failure | 专门模拟服务端失败 |

这些状态码都是服务收到请求后的响应。具体原因还需要看正文、接口规则与日志。当前验证顺序先检查媒体类型，所以同时使用 text/plain 和损坏正文时优先得到 415；不要忽略其他输入条件。

## D

默认 curl 已经完成一次 HTTP 交换，即使服务返回 404，也通常以 0 退出。--fail 将该 HTTP 失败映射为非零退出码，本实验是 22。

fetch 收到 404 时通常仍得到 Response 对象，response.ok 为 false；需要程序主动检查状态。连接失败时才会走相应异常路径，通常没有服务器返回的 HTTP 状态码。网络、浏览器和实际客户端行为还可能影响异常分类，不应只按一个数字猜原因。

## E

返回 422。本服务约定从 JSON 正文读取 title，不会自动把 URL query 中的同名字段合并到正文。输入的位置是契约的一部分。

## F

DELETE 返回 405，Allow 为 GET, POST。curl -i 仍使用当前请求方法并显示响应头，curl -I 会改为 HEAD；本服务不支持 HEAD，所以是 405 且没有响应正文。

Cache-Control 为 no-store，浏览器不应存储本响应用于缓存复用。本服务没有实现条件请求与 304，后续可以用独立实验再学习缓存。

参考答案帮助核对理解；笔记中应保留自己的实际请求与结果。
