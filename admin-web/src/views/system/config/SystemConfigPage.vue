<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchSystemConfig, type SystemConfigSummary } from '@/api/system'

const loading = ref(false)
const error = ref('')
const config = ref<SystemConfigSummary | null>(null)

const configItems = computed(() => {
  if (!config.value) {
    return []
  }

  return [
    { key: '平台名称', value: config.value.platformName },
    { key: '当前阶段', value: config.value.mvpPhase },
    { key: '积分体系', value: config.value.pointsEnabled ? '已开启' : '已关闭' },
    { key: '作品重传', value: config.value.submissionReuploadEnabled ? '允许覆盖最新版本' : '关闭' }
  ]
})

async function loadConfig() {
  loading.value = true
  error.value = ''
  try {
    config.value = await fetchSystemConfig()
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载系统配置失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadConfig()
})
</script>

<template>
  <section class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Config</p>
        <h1>系统配置</h1>
        <p>当前已经接入后台真实配置接口，用于查看 MVP 阶段关键开关和展示配置。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadConfig">
        {{ loading ? '刷新中...' : '刷新配置' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>

    <section class="panel">
      <article v-if="!loading && configItems.length === 0" class="config-item">
        <span>配置状态</span>
        <strong>暂无配置数据</strong>
      </article>
      <article v-for="item in configItems" :key="item.key" class="config-item">
        <span>{{ item.key }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>
  </section>
</template>

<style scoped>
.page {
  min-height: 100%;
  padding: 32px;
  color: #1f2f3c;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 12px;
  color: #9f6230;
  font-size: 14px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 0 0 12px;
  font-size: 36px;
}

.hero p:last-child {
  max-width: 760px;
  margin: 0;
  color: #56697a;
  line-height: 1.7;
}

.ghost-button {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
  background: #eef3f7;
  color: #314657;
  font: inherit;
  cursor: pointer;
}

.message {
  margin: 0 0 16px;
  padding: 12px 14px;
  border-radius: 14px;
  font-size: 14px;
}

.error-message {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.panel {
  display: grid;
  gap: 16px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(33, 50, 68, 0.08);
}

.config-item span {
  color: #56697a;
}

.config-item strong {
  color: #9f6230;
}
</style>
