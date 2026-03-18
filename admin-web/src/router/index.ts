import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAdminSessionStore } from '@/stores/admin-session'
import { pinia } from '@/stores/pinia'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    meta: {
      title: '后台登录'
    },
    component: () => import('@/views/login/LoginPage.vue')
  },
  {
    path: '/',
    meta: {
      requiresAuth: true
    },
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: '',
        meta: {
          title: '平台总览'
        },
        component: () => import('@/views/dashboard/DashboardPage.vue')
      },
      {
        path: 'system/users',
        meta: {
          title: '用户治理'
        },
        component: () => import('@/views/system/users/UserListPage.vue')
      },
      {
        path: 'system/roles',
        meta: {
          title: '角色权限'
        },
        component: () => import('@/views/system/roles/RoleListPage.vue')
      },
      {
        path: 'system/campuses',
        meta: {
          title: '校区管理'
        },
        component: () => import('@/views/system/campuses/CampusListPage.vue')
      },
      {
        path: 'competition/editor',
        meta: {
          title: '比赛发布'
        },
        component: () => import('@/views/competition/CompetitionEditorPage.vue')
      },
      {
        path: 'competition/registrations',
        meta: {
          title: '参赛管理'
        },
        component: () => import('@/views/competition/RegistrationManagePage.vue')
      },
      {
        path: 'review/workbench',
        meta: {
          title: '评审工作台'
        },
        component: () => import('@/views/review/ReviewWorkbenchPage.vue')
      },
      {
        path: 'system/banner',
        meta: {
          title: '轮播图管理'
        },
        component: () => import('@/views/system/banner/BannerPage.vue')
      },
      {
        path: 'system/config',
        meta: {
          title: '系统配置'
        },
        component: () => import('@/views/system/config/SystemConfigPage.vue')
      },
      {
        path: 'system/audit',
        meta: {
          title: '本地审核'
        },
        component: () => import('@/views/system/audit/AuditViolationPage.vue')
      },
      {
        path: 'system/logs',
        meta: {
          title: '操作日志'
        },
        component: () => import('@/views/system/logs/OperationLogPage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const sessionStore = useAdminSessionStore(pinia)

  if (to.path === '/login' && sessionStore.isLoggedIn) {
    return '/'
  }

  if (to.matched.some((record) => record.meta.requiresAuth) && !sessionStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  return true
})

export default router
