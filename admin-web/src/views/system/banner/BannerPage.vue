<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { fetchBanners, type BannerItem, updateBanner } from '@/api/system'

const banners = ref<BannerItem[]>([])
const selectedBannerId = ref<number | null>(null)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  title: '',
  status: 'ENABLED',
  jumpPath: ''
})

function clearNotice() {
  error.value = ''
  success.value = ''
}

function applyBanner(banner: BannerItem | null) {
  selectedBannerId.value = banner?.id ?? null
  form.title = banner?.title ?? ''
  form.status = banner?.status ?? 'ENABLED'
  form.jumpPath = banner?.jumpPath ?? ''
  clearNotice()
}

async function loadBanners(preferredBannerId?: number | null) {
  loading.value = true
  error.value = ''
  try {
    const nextBanners = await fetchBanners()
    banners.value = nextBanners

    if (preferredBannerId != null) {
      applyBanner(nextBanners.find((banner) => banner.id === preferredBannerId) ?? null)
      return
    }

    if (selectedBannerId.value != null) {
      applyBanner(nextBanners.find((banner) => banner.id === selectedBannerId.value) ?? null)
      return
    }

    applyBanner(nextBanners[0] ?? null)
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载轮播图失败'
  } finally {
    loading.value = false
  }
}

function validateForm() {
  if (!form.title.trim()) {
    return '轮播图标题不能为空'
  }
  if (!form.jumpPath.trim()) {
    return '跳转路径不能为空'
  }
  return ''
}

async function handleSubmit() {
  clearNotice()
  if (selectedBannerId.value == null) {
    error.value = '请选择一个轮播图'
    return
  }

  const validationMessage = validateForm()
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  saving.value = true
  try {
    const updated = await updateBanner(selectedBannerId.value, {
      title: form.title.trim(),
      status: form.status,
      jumpPath: form.jumpPath.trim()
    })
    success.value = `已更新轮播图 ${updated.title}`
    await loadBanners(updated.id)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '更新轮播图失败'
  } finally {
    saving.value = false
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
        <p>运营位不再只是查看，选中一条轮播图即可调整标题、状态和跳转路径。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadBanners(selectedBannerId)">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>
    <p v-if="success" class="message success-message">{{ success }}</p>

    <section class="layout">
      <section class="panel list-panel">
        <div class="panel-head">
          <div>
            <h2>轮播图列表</h2>
            <p>{{ banners.length }} 条运营位配置。</p>
          </div>
        </div>

        <section class="grid">
          <article v-if="!loading && banners.length === 0" class="card empty-card">
            <h2>暂无轮播图</h2>
            <p>后端返回为空时，这里会显示空态。</p>
          </article>
          <button
            v-for="item in banners"
            :key="item.id"
            class="card banner-card"
            :class="{ active: item.id === selectedBannerId }"
            type="button"
            @click="applyBanner(item)"
          >
            <h3>{{ item.title }}</h3>
            <p>跳转路径：{{ item.jumpPath }}</p>
            <strong>{{ item.status === 'ENABLED' ? '启用中' : '已停用' }}</strong>
          </button>
        </section>
      </section>

      <section class="panel form-panel">
        <div class="panel-head compact-head">
          <div>
            <h2>{{ selectedBannerId != null ? `编辑轮播图 #${selectedBannerId}` : '请选择轮播图' }}</h2>
            <p>适合快速维护首页曝光位和跳转落点。</p>
          </div>
        </div>

        <template v-if="selectedBannerId != null">
          <label class="field">
            <span>轮播图标题</span>
            <input v-model="form.title" type="text" placeholder="请输入轮播图标题" />
          </label>

          <label class="field">
            <span>状态</span>
            <select v-model="form.status">
              <option value="ENABLED">启用</option>
              <option value="DISABLED">停用</option>
            </select>
          </label>

          <label class="field">
            <span>跳转路径</span>
            <input v-model="form.jumpPath" type="text" placeholder="例如 /pages/home/index" />
          </label>

          <button class="primary-button full-button" type="button" :disabled="saving" @click="handleSubmit">
            {{ saving ? '保存中...' : '保存轮播图配置' }}
          </button>
        </template>

        <p v-else class="empty-text">从左侧选中一条轮播图后，这里会显示可编辑表单。</p>
      </section>
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

.hero h1,
h2,
h3 {
  margin: 0;
}

.hero h1 {
  margin-bottom: 12px;
  font-size: 36px;
}

.hero p:last-child,
.card p,
.panel-head p,
.empty-text {
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

.success-message {
  background: rgba(45, 133, 91, 0.12);
  color: #17603d;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 18px;
}

.panel {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(33, 50, 68, 0.08);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.compact-head {
  margin-bottom: 20px;
}

.grid {
  display: grid;
  gap: 16px;
}

.card {
  padding: 20px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid transparent;
}

.banner-card {
  text-align: left;
  cursor: pointer;
}

.banner-card.active {
  border-color: rgba(204, 109, 55, 0.26);
  background: linear-gradient(180deg, rgba(255, 247, 240, 0.96), rgba(248, 250, 252, 0.96));
}

.card h3 {
  margin-bottom: 10px;
}

.card strong {
  display: inline-flex;
  margin-top: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(204, 109, 55, 0.12);
  color: #9f6230;
}

.field {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.field span {
  color: #3a4655;
  font-weight: 600;
}

input,
select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d9e1e8;
  border-radius: 14px;
  background: #fbfdff;
  color: #1d2a3a;
  font: inherit;
  box-sizing: border-box;
}

.primary-button {
  border: none;
  border-radius: 14px;
  padding: 11px 16px;
  background: linear-gradient(135deg, #cc6d37 0%, #e58f45 100%);
  color: #fff;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.full-button {
  width: 100%;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
