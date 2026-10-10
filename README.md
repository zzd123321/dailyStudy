# dailyStudy

从前端到 AI 应用全栈的静态知识网站。Java 基础 → PostgreSQL 与 Spring Boot → Python 与大模型 → RAG、Agent 与工程交付。

课程集中在 `site/`：每课一页，包含原理、代码、练习、可展开答案和资料链接。提供导航、搜索、页内目录、代码高亮与复制、深浅色切换及移动端布局。

## 本地启动

Node.js 22+，在仓库根目录执行：

```bash
npm ci
npm run dev
```

打开终端显示的地址，通常为 **http://localhost:5173/dailyStudy/**。

```bash
npm run build
npm run preview
```

生产预览通常为 **http://localhost:4173/dailyStudy/**。静态产物在 `site/.vitepress/dist/`，可部署到静态托管；直接双击 HTML 不等同于通过 HTTP 预览。

## 课程与源码

| 课程 | 内容 |
|---|---|
| [Java 01 · 程序结构、编译与运行](site/lessons/java-toolchain.md) | class/main、源码与字节码、变量、命令行参数；包与 Maven 附录 |
| [Java 02 · 基本语法、数组与方法](site/lessons/java-control-flow.md) | 类型、运算、字符串、分支、循环、数组与成绩统计程序 |
| [Java 03 · 类、对象与封装](site/lessons/java-objects.md) | 字段、构造器、this、状态修改、引用与参数传递、static/final |
| [Java 04 · 继承、接口与多态](site/lessons/java-collaboration.md) | 继承与 super、重写、抽象类、接口与组合，以及任务清单 |
| [Java 05 · 集合与任务管理](site/lessons/java-collections.md) | List、Set、Map、过滤、按 ID 管理任务、删除与集合复制 |
| [Java 06 · 泛型、包装类型与对象相等](site/lessons/java-generics-equality.md) | 泛型、装箱与拆箱、equals/hashCode、工作空间内的稳定任务键 |
| [Web A · HTTP 请求与响应](site/lessons/http.md) | HTTP 消息、JSON、方法、状态码、Fetch、跨源与缓存 |
| [Web B · 请求处理与数据保存](site/lessons/request-lifecycle.md) | 服务进程、路由、解析与校验、内存、文件与重启实验 |
| [教材与学习路线](site/learning-path.md) | 教材比较、Java 连续主线、数据库与 AI 后续章节 |

目前有六篇 Java 基础与两篇 Web 专题。内容参考大学 MOOC、官方文档与中文开源教材重新组织，每篇提供对应阅读链接。页面地址保持可用，现有源码包路径沿用历史编号，新任务管理器使用 collections 包，任务键示例使用 types 包。

```text
site/                         每课一份 Markdown
site/.vitepress/              导航、搜索、主题与构建配置
lessons/day-001/examples/     Java 入门示例
lessons/day-003/examples/     旧版语法与输入示例
projects/java-foundations/   Java 基础演示与测试
projects/http-playground/    HTTP 本地实验服务
.github/workflows/pages.yml  静态构建与发布
scripts/setup-cloud-java.sh 云环境工具安装
```

旧版拆开的课程、练习、答案、学习记录模板和每日日程已合并或移除，原内容可从 Git 历史查看。Java 示例路径保持可用。

## GitHub Pages 发布

工作流已配置。首次启用时，在仓库 **Settings → Pages → Build and deployment → Source** 选择 **GitHub Actions**，然后在 **Actions → Deploy knowledge site → Run workflow** 运行一次。

后续 main 分支的网站更新会自动构建并发布。成功后的地址为 **https://zzd123321.github.io/dailyStudy/**，是否可用以 Actions 部署结果为准；配置不代表已经启用 Pages。

当前 base 为 `/dailyStudy/`。部署到自定义域名根目录时，把 `site/.vitepress/config.mts` 的 base 改为 `/`，并调整 favicon 路径。

## 维护课程

在 `site/lessons/` 新增 Markdown，更新 `site/.vitepress/config.mts` 导航。先说明概念，再提供小例子、执行结果与独立练习，注明具体参考章节，用 `::: details` 折叠进阶说明与答案，VitePress snippet 引用现有源码。参考公开教程时标明来源，核对版本，不复制整篇内容。

发布前执行 `npm run build`，构建会检查内部链接。模型密钥和用户数据不能放进静态网站，它们会被发送给浏览器。

## 云环境

使用现有检出，npm 缓存放在工作区：

```bash
cd /workspace/dailyStudy
npm ci --cache /workspace/.npm-cache
npm run build
```

需要验证 Java 示例时：

```bash
bash scripts/setup-cloud-java.sh
source /workspace/.dailystudy-tools/env.sh
cd projects/java-foundations
mvn -B -ntp test
```

云环境安装脚本保留官方下载校验、平台代理和系统证书信任。
