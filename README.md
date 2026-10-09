# dailyStudy：从前端到 AI 应用全栈

学习者背景：三年前端经验，Java/Python 基础薄弱；每周四次，每次两到三小时；本地学习使用 macOS。

主线：Java 业务后端 → Python AI 服务 → 大模型 API → RAG 与工具调用 → 部署与求职项目。以约 48 个学习周推进，并预留补课和返工时间。

## 从这里开始

| 入口 | 内容 |
|---|---|
| [Day 001 完整课程](lessons/day-001/README.md) | Java 工具链、编译运行、学习时长计算器、Maven、练习与验收 |
| [Day 001 练习](lessons/day-001/exercises.md) | 先预测、再运行、解释差异 |
| [每日安排](docs/daily-schedule.md) | 192 次学习的计划；标出已提供完整课程的日期 |
| [完整路线](docs/roadmap.md) | 阶段目标、验收和资料 |
| [macOS 环境与学习工作流](docs/cross-platform-workflow.md) | macOS 的安装、Git 同步、编码与常见错误 |
| [学习记录模板](docs/learning-log-template.md) | 记录实际学习结果，便于下一次接着学 |
| [课程验证记录](docs/day-001-validation.md) | 云环境实际运行结果与本地验证边界 |

Day 表示一次学习，不是连续的自然日。按照每周四次的节奏，Day 001–004 对应第 1 周，Day 005–008 对应第 2 周。

**目前已提供完整教学内容的是 Day 001。** 后续 191 次有计划，尚未标记为完成的课程；它们会围绕当天产出依次展开。仓库提供代码和验证结果，不代表学习者已经掌握。

## 项目组织

```text
docs/                         路线、每日安排、同步说明
lessons/day-001/               当天讲解、示例、练习、参考答案
projects/java-foundations/    从 Java 小程序开始积累的项目代码
notes/                        你自己的学习记录
scripts/setup-cloud-java.sh  Linux x64 云环境工具安装脚本
```

Day 001 的示例和项目不依赖模型 API、数据库或收费服务。后续通过阶段验收后再添加依赖。

## 本地快速开始

先按 [macOS 安装说明](docs/cross-platform-workflow.md) 安装 Java 21 **JDK**、Maven 和 Git。终端确认 `java -version`、`javac -version`、`mvn -version` 正常。

```shell
git clone https://github.com/zzd123321/dailyStudy.git
cd dailyStudy
cd lessons/day-001
javac -encoding UTF-8 -d out examples/HelloStudy.java
java -cp out HelloStudy
```

再从仓库根目录执行：

```shell
cd projects/java-foundations
mvn -B -ntp compile
java -cp target/classes com.dailystudy.day001.LearningBudget
```

这些命令在 macOS 终端执行。路径使用 `/`；不要把命令行提示符复制进去。完整解释见课程。

## 云环境快速开始

云环境里使用现有 `/workspace/dailyStudy` 检出，无需新建 Git worktree。

```bash
cd /workspace/dailyStudy
bash scripts/setup-cloud-java.sh
source /workspace/.dailystudy-tools/env.sh
cd projects/java-foundations
mvn -B -ntp compile
java -cp target/classes com.dailystudy.day001.LearningBudget
```

云环境脚本只支持 Linux x86_64，使用官方 JDK/Maven 下载地址并验证 SHA-256/SHA-512。工具和 Maven 缓存存放在 `/workspace/.dailystudy-tools`，不进入项目仓库。它在本机生成 Maven 代理设置，复用平台提供的系统 Java 信任库，不关闭 TLS 验证。macOS 本地安装按上面的说明操作。

## 怎么判断今天学会了

完成课程后的闭卷小练习、解释题和验收清单，再把实际结果记入 `notes/`。遇到问题，记录执行目录、命令、完整错误、预期与实际结果；后续课程据此补缺。

不要把 API 密钥、密码或真实凭据放入学习记录。模型功能出现之前不需要配置这些信息。
