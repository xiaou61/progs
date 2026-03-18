const HOME_MENU_TITLE_MAP = {
  'teacher-dashboard': '老师看板',
  'competition-list': '比赛列表',
  'message-center': '消息中心',
  'my-points': '我的积分',
  'competition-manage': '发布比赛',
  'my-profile': '个人中心'
}

function buildHomeMenus(roleCode) {
  return roleCode === 'TEACHER'
    ? ['teacher-dashboard', 'competition-list', 'message-center', 'my-points', 'competition-manage', 'my-profile']
    : ['competition-list', 'message-center', 'my-points', 'my-profile']
}

function resolveHomeRoute(menuKey) {
  if (menuKey === 'teacher-dashboard') {
    return '/pages/teacher/dashboard/index'
  }
  if (menuKey === 'competition-list') {
    return '/pages/competition/list/index'
  }
  if (menuKey === 'message-center') {
    return '/pages/message/index'
  }
  if (menuKey === 'my-points') {
    return '/pages/points/index'
  }
  if (menuKey === 'competition-manage') {
    return '/pages/teacher/competition-editor/index'
  }
  if (menuKey === 'my-profile') {
    return '/pages/profile/index'
  }
  return '/pages/home/index'
}

module.exports = {
  HOME_MENU_TITLE_MAP,
  buildHomeMenus,
  resolveHomeRoute
}
