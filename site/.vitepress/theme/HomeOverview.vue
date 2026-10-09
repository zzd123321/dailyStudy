<script setup>
import { withBase } from 'vitepress'

const courses = [
  { number: '01', topic: 'JAVA · 工具链', title: '代码如何成为一个程序',
    description: '先打印一句问候，再改一个计算器。通过实际结果理解编译、运行、JVM 与 Maven。',
    link: '/lessons/java-toolchain', tags: ['JDK', '编译与运行', 'Maven'] },
  { number: '02', topic: 'WEB · 请求链路', title: '看懂一次 HTTP 交换',
    description: '查询和创建一条学习笔记，观察请求与响应，再用三种错误理解格式、解析与校验。',
    link: '/lessons/http', tags: ['接口契约', '状态码', '错误诊断'] },
  { number: '03', topic: 'JAVA · 编程基础', title: '让程序根据输入做决定',
    description: '从固定数字走到用户输入，逐步学会判断、循环、方法，以及输错后怎样重新读取。',
    link: '/lessons/java-control-flow', tags: ['输入校验', '控制流', '方法'] },
  { number: '04', topic: 'WEB · 服务端思维', title: '把请求与数据状态串起来',
    description: '跟着一条笔记从创建走到保存，亲手比较刷新、重复提交和服务重启后的数据。',
    link: '/lessons/request-lifecycle', tags: ['请求链路', '内存与持久化', '故障定位'] }
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
