const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('submission service should upload local file and return file url', async () => {
  delete require.cache[require.resolve('../services/submission')]

  global.wx = {
    _storage: {
      'campus-competition-session': {
        userId: 2,
        roleCode: 'STUDENT',
        token: 'token-2'
      }
    },
    getStorageSync(key) {
      return this._storage[key]
    },
    uploadFile(options) {
      assert.equal(options.url, 'http://127.0.0.1:8080/api/app/submission-files')
      assert.equal(options.name, 'file')
      assert.equal(options.filePath, 'C:/tmp/work.pdf')
      assert.equal(options.header.Authorization, 'Bearer token-2')
      options.success({
        statusCode: 200,
        data: JSON.stringify({
          code: 0,
          data: {
            fileName: 'work.pdf',
            fileUrl: '/uploads/submissions/work.pdf',
            size: 1024
          }
        })
      })
    }
  }

  const { uploadCompetitionWorkFile } = require('../services/submission')
  const result = await uploadCompetitionWorkFile({
    filePath: 'C:/tmp/work.pdf',
    name: 'work.pdf'
  })

  assert.deepEqual(result, {
    fileName: 'work.pdf',
    fileUrl: '/uploads/submissions/work.pdf',
    size: 1024
  })
})

test('submission page should guide users with clear upload steps', () => {
  const source = readFileSync(resolve(__dirname, '../pages/competition/submission/index.js'), 'utf-8')
  const templateSource = readFileSync(resolve(__dirname, '../pages/competition/submission/index.wxml'), 'utf-8')

  assert.match(templateSource, /第 1 步/)
  assert.match(templateSource, /第 2 步/)
  assert.match(templateSource, /第 3 步/)
  assert.match(templateSource, /已选文件/)
  assert.match(templateSource, /提交作品记录/)
  assert.match(templateSource, /最新版本会作为评审依据/)
  assert.match(source, /selectedFileSummary:/)
})
