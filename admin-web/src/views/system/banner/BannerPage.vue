<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchBanners, type BannerItem } from '@/api/system'

const banners = ref<BannerItem[]>([])
const loading = ref(false)
const error = ref('')

async function loadBanners() {
  loading.value = true
  error.value = ''
  try {
    banners.value = await fetchBanners()
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载轮播图失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadBanners()
})
</script>

<template>
  <section class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Banner</p>
        <h1>轮播图管理</h1>
        <p>这里已经接入后台真实轮播图接口，可直接查看当前首页运营位配置。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadBanners">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>

    <section class="grid">
      <article v-if="!loading && banners.length === 0" class="card empty-card">
        <h2>暂无轮播图</h2>
        <p>后端返回为空时，这里会显示空态。</p>
      </article>
      <article v-for="item in banners" :key="item.id" class="card">
        <h2>{{ item.title }}</h2>
        <p>跳转路径：{{ item.jumpPath }}</p>
        <strong>{{ item.status }}</strong>
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

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.card {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(33, 50, 68, 0.08);
}

.card h2 {
  margin: 0 0 10px;
}

.card p {
  margin: 0 0 12px;
  color: #56697a;
}

.card strong {
  color: #9f6230;
}

.empty-card p {
  margin-bottom: 0;
}
</style>
