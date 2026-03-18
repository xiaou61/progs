export type AdminMenuItem = {
  key: string
  title: string
  path: string
}

export type AdminMenuSection = {
  title: string
  items: AdminMenuItem[]
}

export const ADMIN_MENU_SECTIONS: AdminMenuSection[] = [
  {
    title: '工作台',
    items: [
      { key: 'dashboard', title: '平台总览', path: '/' }
    ]
  },
  {
    title: '比赛管理',
    items: [
      { key: 'competition-editor', title: '比赛发布', path: '/competition/editor' },
      { key: 'competition-registrations', title: '参赛管理', path: '/competition/registrations' }
    ]
  },
  {
    title: '评审与结果',
    items: [
      { key: 'review-workbench', title: '评审工作台', path: '/review/workbench' }
    ]
  },
  {
    title: '系统治理',
    items: [
      { key: 'system-users', title: '用户治理', path: '/system/users' },
      { key: 'system-roles', title: '角色权限', path: '/system/roles' },
      { key: 'system-campuses', title: '校区管理', path: '/system/campuses' },
      { key: 'system-banner', title: '轮播图管理', path: '/system/banner' },
      { key: 'system-config', title: '系统配置', path: '/system/config' },
      { key: 'system-logs', title: '操作日志', path: '/system/logs' }
    ]
  },
  {
    title: '内容审核',
    items: [
      { key: 'system-audit', title: '本地审核', path: '/system/audit' }
    ]
  }
]

const MENU_TITLE_BY_PATH = new Map(
  ADMIN_MENU_SECTIONS.flatMap((section) => section.items.map((item) => [item.path, item.title]))
)

export function resolveMenuTitleByPath(path: string) {
  return MENU_TITLE_BY_PATH.get(path) ?? '后台管理'
}
