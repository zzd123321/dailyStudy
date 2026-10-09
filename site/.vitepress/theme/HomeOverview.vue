<script setup>
import { withBase } from 'vitepress'

const courses = [
  { number: '01', topic: 'JAVA 基础', title: '程序结构、编译与运行',
    description: '第一个 Java 程序，class 与 main，源码与字节码，变量、参数和编译错误。',
    link: '/lessons/java-toolchain', tags: ['程序结构', 'javac / java', '包与 Maven'] },
  { number: '02', topic: 'JAVA 基础', title: '基本语法、数组与方法',
    description: '学习变量、运算、字符串、分支与循环，用成绩统计练习数组、输入和返回值。',
    link: '/lessons/java-control-flow', tags: ['类型与运算', '控制流', '数组与方法'] },
  { number: '03', topic: 'JAVA 基础', title: '类、对象与封装',
    description: '创建任务对象，学习字段、构造器和实例方法，分析引用、参数传递与状态修改。',
    link: '/lessons/java-objects', tags: ['类与对象', '构造器', '引用与封装'] },
  { number: '04', topic: 'JAVA 基础', title: '继承、接口与多态',
    description: '从员工与消息输出的小程序理解 super、重写和接口，再应用到任务清单。',
    link: '/lessons/java-collaboration', tags: ['继承与重写', '接口与多态', '组合'] },
  { number: 'A', topic: 'WEB 专题', title: 'HTTP 请求与响应',
    description: '查询和创建笔记，观察方法、URL、头与正文，练习状态检查和 Fetch 错误处理。',
    link: '/lessons/http', tags: ['HTTP 消息', '状态码', 'Fetch'] },
  { number: 'B', topic: 'WEB 专题', title: '请求处理与数据保存',
    description: '观察路由、解析、校验与存储，用刷新、重启和文件实验区分数据的保存位置。',
    link: '/lessons/request-lifecycle', tags: ['服务进程', '路由与校验', '内存与文件'] }
]
</script>

<template>
  <section class="course-overview" aria-labelledby="courses-title">
    <div class="section-intro">
      <div><p class="eyebrow">CONTENTS</p><h2 id="courses-title">课程目录</h2></div>
      <p>先读 Java 01–04，Web 内容单独阅读。<br>每篇包含讲解、代码、练习和参考教材。</p>
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
      <div><p class="eyebrow">READING LIST</p><h2>教材与后续章节</h2></div>
      <p>Java 基础 <span>→</span> 数据库与 Spring Boot <span>→</span> Python 与大模型 <span>→</span> RAG、Agent 与交付</p>
      <a :href="withBase('/learning-path')">查看完整学习路线 <span aria-hidden="true">→</span></a>
    </div>
  </section>
</template>
