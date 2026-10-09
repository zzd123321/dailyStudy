import DefaultTheme from 'vitepress/theme'
import HomeOverview from './HomeOverview.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) { app.component('HomeOverview', HomeOverview) }
}
