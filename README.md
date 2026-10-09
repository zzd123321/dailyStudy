# dailyStudy

从前端到 AI 应用全栈的静态知识网站。Java 业务后端 → Python AI 服务 → 大模型应用 → RAG 与可靠性。

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
| [01 · Java 工具链与程序执行](site/lessons/java-toolchain.md) | JDK/JVM、编译与运行、包、classpath、Maven 生命周期 |
| [02 · HTTP 请求、响应与诊断](site/lessons/http.md) | 契约、方法、JSON、同源、curl、状态码与故障分层 |
| [03 · Java 输入、控制流与方法](site/lessons/java-control-flow.md) | 解析校验、边界、短路、循环状态、方法与交互程序 |
| [学习路线](site/learning-path.md) | 后续 Java、数据库、Python 与 AI 的知识依赖 |
| [macOS 环境](site/setup.md) | Java/Maven/Git/Node 安装与运行 |

目前有前三课完整内容，后续继续加入同一站点。

```text
site/                         每课一份 Markdown
site/.vitepress/              导航、搜索、主题与构建配置
lessons/day-001/examples/     第 01 课最小 Java 示例
lessons/day-003/examples/     第 03 课最小 Java 示例
projects/java-foundations/   固定计算器与交互式计划器
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

在 `site/lessons/` 新增 Markdown，更新 `site/.vitepress/config.mts` 导航。按“概念 → 推演 → 示例 → 误区 → 练习”组织正文，用 `::: details` 折叠答案，VitePress snippet 引用现有源码。

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

Linux 安装脚本保留官方下载校验、平台代理和系统证书信任。Mac 使用网站里的安装说明。
