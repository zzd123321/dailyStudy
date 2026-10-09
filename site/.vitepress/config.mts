import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'zh-CN',
  title: 'dailyStudy',
  description: '从前端到 AI 应用全栈：Java、Python 与大模型应用的连贯知识课程。',
  base: '/dailyStudy/',
  cleanUrls: true,
  head: [['link', { rel: 'icon', href: '/dailyStudy/favicon.svg', type: 'image/svg+xml' }]],
  markdown: { lineNumbers: true },
  themeConfig: {
    logo: '/favicon.svg',
    siteTitle: 'dailyStudy',
    nav: [
      { text: '课程', link: '/lessons/java-toolchain', activeMatch: '/lessons/' },
      { text: '学习路线', link: '/learning-path' }
    ],
    sidebar: [
      { text: '开始阅读', items: [
        { text: '学习路线', link: '/learning-path' }
      ] },
      { text: '基础篇 · 前端走向服务端', items: [
        { text: '01 · Java 工具链与程序执行', link: '/lessons/java-toolchain' },
        { text: '02 · HTTP 请求、响应与诊断', link: '/lessons/http' },
        { text: '03 · Java 输入、控制流与方法', link: '/lessons/java-control-flow' },
        { text: '04 · 请求链路与服务端状态', link: '/lessons/request-lifecycle' }
      ] },
      { text: 'Java 篇 · 从语法走向业务', items: [
        { text: '05 · Java 类、对象与封装', link: '/lessons/java-objects' },
        { text: '06 · 对象协作、继承与接口', link: '/lessons/java-collaboration' }
      ] }
    ],
    outline: { level: [2, 3], label: '本页内容' },
    search: {
      provider: 'local',
      options: {
        locales: { root: { translations: {
          button: { buttonText: '搜索知识', buttonAriaLabel: '搜索知识' },
          modal: { noResultsText: '没有找到相关内容', resetButtonTitle: '清空搜索',
            footer: { selectText: '选择', navigateText: '切换', closeText: '关闭' } }
        } } }
      }
    },
    socialLinks: [{ icon: 'github', link: 'https://github.com/zzd123321/dailyStudy' }],
    docFooter: { prev: '上一课', next: '继续阅读' },
    darkModeSwitchLabel: '外观',
    darkModeSwitchTitle: '切换到深色',
    lightModeSwitchTitle: '切换到浅色',
    sidebarMenuLabel: '课程目录',
    returnToTopLabel: '返回顶部',
    externalLinkIcon: true,
    footer: { message: '理解原理，写出代码，解释结果。', copyright: 'dailyStudy · AI 应用全栈知识课程' }
  }
})
