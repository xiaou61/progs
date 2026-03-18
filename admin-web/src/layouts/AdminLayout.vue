<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAdminSessionStore } from '@/stores/admin-session'
import { ADMIN_MENU_SECTIONS, resolveMenuTitleByPath } from '@/utils/admin-shell'

const route = useRoute()
const router = useRouter()
const sessionStore = useAdminSessionStore()

const pageTitle = computed(() => {
  if (typeof route.meta.title === 'string') {
    return route.meta.title
  }
  return resolveMenuTitleByPath(route.path)
})

const subtitle = computed(() => `${sessionStore.displayName || sessionStore.studentNo} · ${sessionStore.roleCode || 'ADMIN'}`)

function isActive(path: string) {
  return route.path === path
}

function logout() {
  sessionStore.logout()
  void router.replace('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <p class="eyebrow">Admin Console</p>
        <h1>师生比赛后台</h1>
        <p>左侧导航直达主要治理模块</p>
        <span class="brand-badge">账号开通 · 赛事治理 · 运营配置</span>
      </div>

      <nav class="nav">
        <section v-for="section in ADMIN_MENU_SECTIONS" :key="section.title" class="nav-section">
          <p class="nav-section-title">{{ section.title }}</p>
          <RouterLink
            v-for="item in section.items"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            :class="{ active: isActive(item.path) }"
          >
            {{ item.title }}
          </RouterLink>
        </section>
      </nav>
    </aside>

    <div class="main-shell">
      <header class="topbar">
        <div class="topbar-main">
          <p class="topbar-label">当前页面</p>
          <h2>{{ pageTitle }}</h2>
          <p class="topbar-caption">统一处理账号、比赛、审核与系统配置</p>
        </div>
        <div class="topbar-actions">
          <div class="account-card">
            <strong>{{ sessionStore.displayName || '管理员' }}</strong>
            <span>{{ subtitle }}</span>
          </div>
          <button class="logout-button" type="button" @click="logout">退出登录</button>
        </div>
      </header>

      <main class="content">
        <div class="content-surface">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(98, 145, 181, 0.14), transparent 24%),
    linear-gradient(160deg, #eff4f8 0%, #f6f2eb 100%);
}

.sidebar {
  padding: 24px 20px;
  border-right: 1px solid rgba(50, 72, 92, 0.08);
  background: rgba(21, 38, 52, 0.96);
  color: #f0f5f8;
}

.brand {
  margin-bottom: 24px;
  padding: 20px;
  border-radius: 20px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.03)),
    rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.eyebrow,
.brand h1,
.brand p,
.brand-badge,
.nav-section-title,
.nav-link,
.topbar-label,
.topbar h2,
.topbar-caption,
.account-card strong,
.account-card span {
  margin: 0;
}

.eyebrow {
  margin-bottom: 10px;
  color: #f7bf8b;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.brand h1 {
  margin-bottom: 10px;
  font-size: 24px;
}

.brand p {
  color: rgba(240, 245, 248, 0.7);
  line-height: 1.6;
}

.brand-badge {
  display: inline-flex;
  margin-top: 14px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(247, 191, 139, 0.14);
  color: #f7d3ad;
  font-size: 12px;
}

.nav {
  display: grid;
  gap: 18px;
}

.nav-section {
  display: grid;
  gap: 8px;
}

.nav-section-title {
  color: rgba(240, 245, 248, 0.55);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.nav-link {
  display: block;
  padding: 12px 14px;
  border-radius: 14px;
  color: rgba(240, 245, 248, 0.82);
  text-decoration: none;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.nav-link:hover,
.nav-link.active {
  background: rgba(247, 191, 139, 0.16);
  color: #ffffff;
  transform: translateX(2px);
}

.main-shell {
  min-width: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  padding: 24px 28px 16px;
}

.topbar-main {
  display: grid;
  gap: 6px;
}

.topbar-label {
  margin-bottom: 6px;
  color: #667a8d;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.topbar h2 {
  color: #173149;
  font-size: 30px;
}

.topbar-caption {
  color: #6d8194;
  font-size: 14px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.account-card {
  display: grid;
  gap: 4px;
  padding: 12px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 42px rgba(23, 47, 74, 0.08);
}

.account-card strong {
  color: #1d2a3a;
}

.account-card span {
  color: #627586;
  font-size: 13px;
}

.logout-button {
  border: none;
  border-radius: 999px;
  padding: 12px 16px;
  background: #dce8f1;
  color: #426682;
  font: inherit;
  cursor: pointer;
}

.content {
  min-width: 0;
  padding: 0 28px 28px;
}

.content-surface {
  min-height: calc(100vh - 120px);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.22);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid rgba(50, 72, 92, 0.08);
  }

  .content {
    padding-inline: 20px;
    padding-bottom: 20px;
  }
}
</style>
