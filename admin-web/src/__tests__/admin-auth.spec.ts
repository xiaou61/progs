import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

describe('admin session store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('persists and restores admin session', async () => {
    const { useAdminSessionStore } = await import('../stores/admin-session')

    const store = useAdminSessionStore()
    store.applyLogin({
      token: 'dev-token-1',
      userId: 1,
      roleCode: 'ADMIN',
      studentNo: 'A20260001',
      displayName: '系统管理员'
    })

    expect(store.isLoggedIn).toBe(true)
    expect(store.displayName).toBe('系统管理员')

    const restoredStore = useAdminSessionStore()
    restoredStore.hydrate()

    expect(restoredStore.token).toBe('dev-token-1')
    expect(restoredStore.roleCode).toBe('ADMIN')
    expect(restoredStore.studentNo).toBe('A20260001')
  })
})
