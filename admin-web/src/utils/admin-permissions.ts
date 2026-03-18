type AdminNavigationItem = {
  path: string
  label: string
  permissionCode?: string
}

const adminNavigation: AdminNavigationItem[] = [
  { path: '/', label: '工作台' },
  { path: '/competition/editor', label: '比赛发布', permissionCode: 'COMPETITION_MANAGE' },
  { path: '/competition/registrations', label: '报名管理', permissionCode: 'COMPETITION_MANAGE' },
  { path: '/review/workbench', label: '评审工作台', permissionCode: 'REVIEW_MANAGE' },
  { path: '/system/users', label: '用户管理', permissionCode: 'USER_MANAGE' },
  { path: '/system/roles', label: '角色权限', permissionCode: 'ROLE_MANAGE' },
  { path: '/system/campuses', label: '校区管理', permissionCode: 'CAMPUS_MANAGE' },
  { path: '/system/banner', label: '轮播图管理', permissionCode: 'SYSTEM_CONFIG' },
  { path: '/system/config', label: '系统配置', permissionCode: 'SYSTEM_CONFIG' },
  { path: '/system/audit', label: '本地审核', permissionCode: 'AUDIT_VIEW' },
  { path: '/system/logs', label: '操作日志', permissionCode: 'LOG_VIEW' }
]

export function buildAdminNavigation(permissionCodes: string[]) {
  return adminNavigation.filter((item) => !item.permissionCode || permissionCodes.includes(item.permissionCode))
}

export function canAccessAdminRoute(path: string, permissionCodes: string[]) {
  const matched = adminNavigation.find((item) => item.path === path)
  if (!matched?.permissionCode) {
    return true
  }
  return permissionCodes.includes(matched.permissionCode)
}
