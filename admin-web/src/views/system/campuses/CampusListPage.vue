<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchCampuses, type CampusItem } from '@/api/campuses'

const campuses = ref<CampusItem[]>([])
const loading = ref(false)
const error = ref('')

async function loadCampuses() {
  loading.value = true
  error.value = ''
  try {
    campuses.value = await fetchCampuses()
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载校区列表失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadCampuses()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">System</p>
        <h1>校区管理</h1>
        <p>这里已经接入后台校区接口，当前可查看已启用校区与编码信息。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadCampuses">
        {{ loading ? '刷新中...' : '刷新校区' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>

    <section class="list">
      <article v-if="!loading && campuses.length === 0" class="item">
        <h2>暂无校区</h2>
        <p>当前没有可展示的校区数据。</p>
      </article>
      <article v-for="campus in campuses" :key="campus.id" class="item">
        <h2>{{ campus.campusName }}</h2>
        <p>编码：{{ campus.campusCode }}</p>
        <span>{{ campus.status }}</span>
      </article>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px;
  background: linear-gradient(180deg, #f8f1e8 0%, #eef4f8 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #b1612a;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 12px;
}

h1 {
  margin: 0 0 8px;
  color: #1d2a3a;
}

.header p:last-child {
  margin: 0;
  color: #526171;
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

.list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.item {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

h2 {
  margin: 0 0 12px;
}

p,
span {
  color: #526171;
}
</style>
