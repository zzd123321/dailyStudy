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
      { text: 'Java 基础', items: [
        { text: '01 · 程序结构、编译与运行', link: '/lessons/java-toolchain' },
        { text: '02 · 基本语法、数组与方法', link: '/lessons/java-control-flow' },
        { text: '03 · 类、对象与封装', link: '/lessons/java-objects' },
        { text: '04 · 继承、接口与多态', link: '/lessons/java-collaboration' },
        { text: '05 · 集合与任务管理', link: '/lessons/java-collections' },
        { text: '06 · 泛型、包装类型与对象相等', link: '/lessons/java-generics-equality' }
      ] },
      { text: 'Web 基础专题', items: [
        { text: 'A · HTTP 请求与响应', link: '/lessons/http' },
        { text: 'B · 请求处理与数据保存', link: '/lessons/request-lifecycle' }
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
    footer: { message: 'Java、Web 与 AI 应用开发', copyright: 'dailyStudy · 教程与示例代码' }
  }
})
