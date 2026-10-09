<script setup>
import { computed, ref } from 'vue'

const steps = ['准备请求', '建立连接', '匹配接口', '解析与校验', '读取 / 写入内存', '生成 HTTP 响应']
const scenarios = [
  { key: 'created', name: '合法创建', request: 'POST /api/notes\nContent-Type: application/json\n\n{"title":"理解请求链路"}', visited: [0, 1, 2, 3, 4, 5], stop: -1, response: 'HTTP 201 · Created', code: '返回新条目与 Location', effect: '增加一条内存数据。再次发送会再创建一条。', why: '正文符合接口契约，服务生成 ID、保存条目，再把结果编码成 JSON。' },
  { key: 'media', name: '正文类型不符', request: 'POST /api/notes\nContent-Type: text/plain\n\n{"title":"HTTP"}', visited: [0, 1, 2, 3, 5], stop: 3, response: 'HTTP 415', code: 'unsupported_media_type', effect: '不增加条目，也不分配新 ID。', why: '先检查 Content-Type，媒体类型不符合接口约定，尚未进行 JSON 解析就返回。' },
  { key: 'json', name: 'JSON 语法错误', request: 'POST /api/notes\nContent-Type: application/json\n\n{"title":', visited: [0, 1, 2, 3, 5], stop: 3, response: 'HTTP 400', code: 'invalid_json', effect: '不增加条目，也不分配新 ID。', why: '媒体类型正确，但正文不是完整 JSON；解析异常被转换成明确的错误响应。' },
  { key: 'title', name: '字段规则不符', request: 'POST /api/notes\nContent-Type: application/json\n\n{"title":"   "}', visited: [0, 1, 2, 3, 5], stop: 3, response: 'HTTP 422', code: 'invalid_title', effect: '不增加条目，也不分配新 ID。', why: 'JSON 解析成功，但 title 去空白后为空，因此在数据写入之前返回。' },
  { key: 'missing', name: '条目不存在', request: 'GET /api/notes/999999', visited: [0, 1, 2, 4, 5], stop: 4, response: 'HTTP 404', code: 'note_not_found', effect: '只查询，不创建数据。', why: '路径和方法命中接口，但在当前内存数据中找不到指定 ID。该 GET 不解析创建用的 JSON。' },
  { key: 'route', name: '接口不存在', request: 'GET /api/unknown', visited: [0, 1, 2, 5], stop: 2, response: 'HTTP 404', code: 'route_not_found', effect: '没有进入条目业务。', why: '没有匹配到接口，服务直接构造 404；这和匹配接口后找不到条目是不同原因。' },
  { key: 'method', name: '方法不支持', request: 'DELETE /api/notes', visited: [0, 1, 2, 5], stop: 2, response: 'HTTP 405', code: 'method_not_allowed · Allow: GET, POST', effect: '没有删除任何条目。', why: '路径已知，但这个接口只接受 GET 和 POST，不能仅凭路径相同就进入创建逻辑。' },
  { key: 'failure', name: '模拟服务端失败', request: 'GET /api/demo/failure', visited: [0, 1, 2, 5], stop: 2, response: 'HTTP 500', code: 'demo_failure', effect: '不改变条目；实验进程仍可处理下一次请求。', why: '本接口故意直接返回 500，用于教学。它没有模拟数据库或模型故障。' },
  { key: 'connection', name: '服务已停止', request: 'GET http://127.0.0.1:8082/api/notes\n目标端口无服务监听', visited: [0, 1], stop: 1, response: '没有 HTTP 响应', code: '连接失败', effect: '请求没有进入这个 Java 服务。', why: '客户端无法建立连接，没有服务端返回的状态码或 JSON。不能把它当成 HTTP 500。' }
]
const selected = ref('created')
const current = computed(() => scenarios.find(item => item.key === selected.value))
const state = index => !current.value.visited.includes(index) ? 'skip' : current.value.stop === index ? 'stop' : 'pass'
const labels = { pass: '已经过', stop: '在此结束或提前返回', skip: '未进入' }
</script>

<template>
  <section class="request-flow" aria-label="请求链路推演">
    <label for="flow-scenario">选择一种请求场景</label>
    <select id="flow-scenario" v-model="selected">
      <option v-for="item in scenarios" :key="item.key" :value="item.key">{{ item.name }}</option>
    </select>
    <p class="flow-caption">按实验服务源码展示处理步骤。实际请求使用正文中的 curl 命令验证。</p>
    <pre class="flow-request"><code>{{ current.request }}</code></pre>
    <ol class="flow-steps">
      <li v-for="(step, index) in steps" :key="step" :class="state(index)">
        <span class="flow-index">{{ index + 1 }}</span>
        <span><strong>{{ step }}</strong><small>{{ labels[state(index)] }}</small></span>
      </li>
    </ol>
    <div class="flow-result" aria-live="polite" aria-atomic="true">
      <p class="flow-status">{{ current.response }}</p>
      <p><code>{{ current.code }}</code></p>
      <p>{{ current.why }}</p>
      <p><strong>数据变化：</strong>{{ current.effect }}</p>
    </div>
  </section>
</template>

<style scoped>
.request-flow { margin: 24px 0; padding: 24px; border: 1px solid var(--vp-c-divider); border-radius: 14px; }
label { display: block; margin-bottom: 10px; font-weight: 600; }
select { appearance: auto; width: 100%; padding: 10px 14px; font: inherit; border: 1px solid var(--vp-c-divider); border-radius: 8px; color: var(--vp-c-text-1); background: var(--vp-c-bg-soft); }
select:focus-visible { outline: 2px solid var(--vp-c-brand-1); outline-offset: 2px; }
.flow-caption { font-size: 13px; color: var(--vp-c-text-2); }
.flow-request { overflow-x: auto; padding: 14px; border-radius: 8px; background: var(--vp-c-bg-alt); font-size: 13px; }
.flow-request code { padding: 0; color: var(--vp-c-text-1); background: transparent; }
.flow-steps { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin: 18px 0; padding: 0; list-style: none; }
.flow-steps li { display: flex; align-items: center; gap: 12px; margin: 0; padding: 12px; border: 1px solid var(--vp-c-divider); border-radius: 8px; }
.flow-index { display: grid; place-items: center; flex-shrink: 0; width: 26px; height: 26px; border-radius: 50%; background: var(--vp-c-bg-soft); font-size: 12px; }
strong { font-size: 13px; }
small { display: block; font-size: 11px; color: var(--vp-c-text-2); }
.pass .flow-index { color: var(--vp-c-brand-1); background: var(--vp-c-brand-soft); }
.stop { border-color: var(--vp-c-brand-1) !important; background: var(--vp-c-brand-soft); }
.skip { background: var(--vp-c-bg-alt); border-style: dashed !important; }
.skip strong { color: var(--vp-c-text-2); }
.flow-result { border-top: 1px solid var(--vp-c-divider); padding-top: 8px; }
.flow-result p { margin: 10px 0; font-size: 14px; }
.flow-result .flow-status { font-weight: 650; font-size: 20px; color: var(--vp-c-brand-1); }
@media (max-width: 639px) { .request-flow { padding: 16px; } .flow-steps { grid-template-columns: 1fr; } }
</style>
