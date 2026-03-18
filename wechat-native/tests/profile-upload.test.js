const test = require('node:test')
const assert = require('node:assert/strict')

test('profile service should upload local image and return file url', async () => {
  delete require.cache[require.resolve('../services/profile')]

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
      assert.equal(options.url, 'http://127.0.0.1:8080/api/app/profile/files')
      assert.equal(options.name, 'file')
      assert.equal(options.filePath, 'C:/tmp/avatar.png')
      assert.equal(options.header.Authorization, 'Bearer token-2')
      options.success({
        statusCode: 200,
        data: JSON.stringify({
          code: 0,
          data: {
            fileName: 'avatar.png',
            fileUrl: '/uploads/profile/avatar.png',
            size: 2048
          }
        })
      })
    }
  }

  const { uploadProfileFile } = require('../services/profile')
  const result = await uploadProfileFile({
    filePath: 'C:/tmp/avatar.png',
    name: 'avatar.png'
  })

  assert.deepEqual(result, {
    fileName: 'avatar.png',
    fileUrl: '/uploads/profile/avatar.png',
    size: 2048
  })
})
