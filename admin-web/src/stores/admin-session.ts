import { defineStore } from 'pinia'

export const ADMIN_SESSION_STORAGE_KEY = 'campus-competition-admin-session'

export type AdminSession = {
  token: string
  userId: number
  roleCode: 'ADMIN'
  studentNo: string
  displayName: string
}

function canUseStorage() {
  return typeof localStorage !== 'undefined'
}

export function loadAdminSession(): AdminSession | null {
  if (!canUseStorage()) {
    return null
  }

  const rawValue = localStorage.getItem(ADMIN_SESSION_STORAGE_KEY)
  if (!rawValue) {
    return null
  }

  try {
    const parsed = JSON.parse(rawValue) as Partial<AdminSession>
    if (
      typeof parsed.token === 'string' &&
      typeof parsed.userId === 'number' &&
      parsed.roleCode === 'ADMIN' &&
      typeof parsed.studentNo === 'string' &&
      typeof parsed.displayName === 'string'
    ) {
      return parsed as AdminSession
    }
  } catch {
    return null
  }

  return null
}

function saveAdminSession(session: AdminSession) {
  if (!canUseStorage()) {
    return
  }
  localStorage.setItem(ADMIN_SESSION_STORAGE_KEY, JSON.stringify(session))
}

function clearAdminSessionStorage() {
  if (!canUseStorage()) {
    return
  }
  localStorage.removeItem(ADMIN_SESSION_STORAGE_KEY)
}

export const useAdminSessionStore = defineStore('admin-session', {
  state: () => ({
    token: '',
    userId: 0,
    roleCode: '' as '' | 'ADMIN',
    studentNo: '',
    displayName: ''
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    session: (state): AdminSession | null => state.token
      ? {
        token: state.token,
        userId: state.userId,
        roleCode: 'ADMIN',
        studentNo: state.studentNo,
        displayName: state.displayName
      }
      : null
  },
  actions: {
    hydrate() {
      const session = loadAdminSession()
      if (!session) {
        return false
      }

      this.token = session.token
      this.userId = session.userId
      this.roleCode = session.roleCode
      this.studentNo = session.studentNo
      this.displayName = session.displayName
      return true
    },
    applyLogin(session: AdminSession) {
      this.token = session.token
      this.userId = session.userId
      this.roleCode = session.roleCode
      this.studentNo = session.studentNo
      this.displayName = session.displayName
      saveAdminSession(session)
    },
    logout() {
      this.token = ''
      this.userId = 0
      this.roleCode = ''
      this.studentNo = ''
      this.displayName = ''
      clearAdminSessionStorage()
    }
  }
})
