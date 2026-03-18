const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('profile page should not require manual avatar or feedback image urls', () => {
  const profileSource = readFileSync(resolve(__dirname, '../pages/profile/index.js'), 'utf-8')
  const profileTemplateSource = readFileSync(resolve(__dirname, '../pages/profile/index.wxml'), 'utf-8')

  assert.equal(profileSource.includes('normalizeImageUrls'), false)
  assert.equal(profileTemplateSource.includes('填写头像 URL'), false)
  assert.equal(profileTemplateSource.includes('每行一个图片 URL'), false)
})
