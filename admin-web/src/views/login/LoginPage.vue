<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useAdminSessionStore } from '@/stores/admin-session'

const router = useRouter()
const route = useRoute()
const sessionStore = useAdminSessionStore()

const loading = ref(false)
const error = ref('')
const form = reactive({
  studentNo: '',
  password: ''
})

const redirectTarget = computed(() => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
  return redirect.startsWith('/') ? redirect : '/'
})

async function submitLogin() {
  error.value = ''
  if (!form.studentNo.trim()) {
    error.value = '管理员账号不能为空'
    return
  }
  if (!form.password.trim()) {
    error.value = '密码不能为空'
    return
  }

  loading.value = true
  try {
    const result = await login({
      studentNo: form.studentNo.trim(),
      password: form.password,
      roleCode: 'ADMIN'
    })

    if (result.roleCode !== 'ADMIN') {
      throw new Error('当前账号不是管理员')
    }

    sessionStore.applyLogin({
      token: result.token,
      userId: result.userId,
      roleCode: 'ADMIN',
      studentNo: form.studentNo.trim(),
      displayName: form.studentNo.trim()
    })

    await router.replace(redirectTarget.value)
  } catch (loginError) {
    error.value = loginError instanceof Error ? loginError.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Campus Competition</p>
        <h1>后台管理登录</h1>
        <p>后台管理端已接入真实认证接口，请使用已开通的管理员账号登录。</p>
      </div>
    </section>

    <section class="form-card">
      <label class="field">
        <span>管理员账号</span>
        <input v-model="form.studentNo" type="text" placeholder="请输入管理员学号" />
      </label>

      <label class="field">
        <span>密码</span>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </label>

      <p v-if="error" class="error-text">{{ error }}</p>

      <button class="primary-button" type="button" :disabled="loading" @click="submitLogin">
        {{ loading ? '登录中...' : '登录进入后台' }}
      </button>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: grid;
  align-content: center;
  gap: 20px;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(255, 199, 145, 0.24), transparent 26%),
    linear-gradient(160deg, #f8f1e8 0%, #e8f0f6 100%);
}

.hero-card,
.form-card {
  width: min(480px, calc(100vw - 48px));
  margin: 0 auto;
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(31, 45, 61, 0.12);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.eyebrow,
h1,
.hero-card p:last-child,
.field span {
  margin: 0;
}

.eyebrow {
  margin-bottom: 10px;
  color: #8a5b2b;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 12px;
}

h1 {
  margin-bottom: 12px;
  color: #1d2a3a;
}

.hero-card p:last-child {
  color: #4d5b6b;
  line-height: 1.7;
}

.field {
  display: grid;
  gap: 8px;
  margin-bottom: 18px;
}

.field span {
  color: #556678;
  font-size: 14px;
}

.field input {
  height: 46px;
  padding: 0 14px;
  border: 1px solid #d9e1e8;
  border-radius: 14px;
  background: #f8fafc;
}

.primary-button,
.ghost-button {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
  font: inherit;
  cursor: pointer;
}

.primary-button {
  width: 100%;
  background: linear-gradient(135deg, #c86c31, #dd8b47);
  color: #fff;
  font-weight: 700;
}

.error-text {
  margin: 0 0 14px;
  color: #b14a2f;
  font-size: 14px;
}
</style>
