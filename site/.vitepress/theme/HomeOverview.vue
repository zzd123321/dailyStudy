<script setup>
import { withBase } from 'vitepress'

const courses = [
  { number: '01', topic: 'JAVA · 工具链', title: '代码如何成为一个程序',
    description: '从问候与计算器理解编译运行，再用包、命令行参数、JAR 与 Maven 完成独立交付。',
    link: '/lessons/java-toolchain', tags: ['JDK', '编译与运行', 'Maven'] },
  { number: '02', topic: 'WEB · 请求链路', title: '看懂一次 HTTP 交换',
    description: '从笔记接口走到方法语义、参数编码、缓存与跨源，写出检查状态和结构的客户端。',
    link: '/lessons/http', tags: ['接口契约', '状态码', '错误诊断'] },
  { number: '03', topic: 'JAVA · 编程基础', title: '让程序根据输入做决定',
    description: '用输入、运算、判断、数组与方法写出统计程序，再练习错误重试和断点调试。',
    link: '/lessons/java-control-flow', tags: ['输入校验', '控制流', '方法'] },
  { number: '04', topic: 'WEB · 服务端思维', title: '把请求与数据状态串起来',
    description: '追踪笔记创建、刷新与重启，验证文件保存，再分析分层、并发、幂等和日志。',
    link: '/lessons/request-lifecycle', tags: ['请求链路', '内存与持久化', '故障定位'] },
  { number: '05', topic: 'JAVA · 业务对象', title: '让对象维护自己的规则',
    description: '从两条任务理解实例、构造器和封装，用改名、完成与重新打开串联状态、引用和 static。',
    link: '/lessons/java-objects', tags: ['类与对象', '封装与状态', '引用传递'] },
  { number: '06', topic: 'JAVA · 对象协作', title: '让多个对象各负其责',
    description: '为同一批任务生成两种清单，串联接口、多态与继承，理解构造器传入依赖和组合的用途。',
    link: '/lessons/java-collaboration', tags: ['接口与多态', '继承与抽象类', '组合与分工'] }
]
</script>

<template>
  <section class="course-overview" aria-labelledby="courses-title">
    <div class="section-intro">
      <div><p class="eyebrow">THE FOUNDATION</p><h2 id="courses-title">从这里建立后端思维</h2></div>
      <p>按顺序理解概念，再用代码检验理解。<br>讲解、示例、练习与答案，都在同一页。</p>
    </div>
    <div class="course-grid">
      <a v-for="course in courses" :key="course.number" :href="withBase(course.link)" class="course-card">
        <div class="card-top"><span class="course-number">{{ course.number }}</span><span class="course-topic">{{ course.topic }}</span></div>
        <h3>{{ course.title }}</h3><p>{{ course.description }}</p>
        <div class="course-tags"><span v-for="tag in course.tags" :key="tag">{{ tag }}</span></div>
        <div class="card-link">阅读这一课 <span aria-hidden="true">↗</span></div>
      </a>
    </div>
    <div class="path-strip">
      <div><p class="eyebrow">THE BIG PICTURE</p><h2>把已有前端能力，接到完整系统。</h2></div>
      <p>Java 基础 <span>→</span> 数据库与 Spring Boot <span>→</span> Python 与大模型 <span>→</span> RAG、Agent 与交付</p>
      <a :href="withBase('/learning-path')">查看完整学习路线 <span aria-hidden="true">→</span></a>
    </div>
  </section>
</template>
