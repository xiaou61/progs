const { request } = require('./http')

function fetchHomeBanners() {
  return request('/api/app/banners')
}

module.exports = {
  fetchHomeBanners
}
