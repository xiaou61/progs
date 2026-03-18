const LOGIN_PAGE_ROUTE = '/pages/login/index'
const HOME_PAGE_ROUTE = '/pages/home/index'

function buildLoginRoute(redirectUrl) {
  if (!redirectUrl) {
    return LOGIN_PAGE_ROUTE
  }
  return `${LOGIN_PAGE_ROUTE}?redirect=${encodeURIComponent(redirectUrl)}`
}

function resolveProtectedRoute(targetUrl, isLoggedIn) {
  return isLoggedIn ? targetUrl : buildLoginRoute(targetUrl)
}

function resolvePostLoginRoute(redirectUrl) {
  if (redirectUrl && /^\/pages\//.test(redirectUrl)) {
    return redirectUrl
  }
  return HOME_PAGE_ROUTE
}

module.exports = {
  HOME_PAGE_ROUTE,
  LOGIN_PAGE_ROUTE,
  buildLoginRoute,
  resolvePostLoginRoute,
  resolveProtectedRoute
}
