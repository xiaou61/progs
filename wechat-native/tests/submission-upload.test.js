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

test('submission page should keep selected file after refreshing submission state', async () => {
  const pagePath = resolve(__dirname, '../pages/competition/submission/index.js')
  const competitionServicePath = resolve(__dirname, '../services/competition.js')
  const submissionServicePath = resolve(__dirname, '../services/submission.js')
  const authPath = resolve(__dirname, '../utils/auth.js')
  let pageConfig = null

  delete require.cache[pagePath]
  delete require.cache[competitionServicePath]
  delete require.cache[submissionServicePath]
  delete require.cache[authPath]

  require.cache[competitionServicePath] = {
    id: competitionServicePath,
    filename: competitionServicePath,
    loaded: true,
    exports: {
      fetchCompetitions: async () => [
        {
          id: 8,
          title: '蓝桥杯',
          description: '作品上传验证'
        }
      ]
    }
  }
  require.cache[submissionServicePath] = {
    id: submissionServicePath,
    filename: submissionServicePath,
    loaded: true,
    exports: {
      fetchCompetitionSubmissions: async () => [],
      submitCompetitionWork: async () => 1,
      uploadCompetitionWorkFile: async () => ({
        fileUrl: '/uploads/submissions/work.pdf'
      })
    }
  }
  require.cache[authPath] = {
    id: authPath,
    filename: authPath,
    loaded: true,
    exports: {
      getSession: () => ({ userId: 2 }),
      requireLogin: () => true
    }
  }

  global.Page = (config) => {
    pageConfig = config
  }

  require(pagePath)

  const pageInstance = {
    data: {
      ...pageConfig.data,
      competitionId: 8,
      selectedFilePath: 'C:/tmp/work.pdf',
      selectedFileName: 'work.pdf',
      selectedFileSizeLabel: '1.0 KB',
      selectedFileSummary: '已选择 work.pdf，大小 1.0 KB'
    },
    setData(update) {
      Object.assign(this.data, update)
    }
  }

  await pageConfig.loadSubmissionState.call(pageInstance)

  assert.equal(pageInstance.data.selectedFilePath, 'C:/tmp/work.pdf')
  assert.equal(pageInstance.data.selectedFileName, 'work.pdf')
  assert.equal(pageInstance.data.selectedFileSummary, '已选择 work.pdf，大小 1.0 KB')

  delete global.Page
})

test('submission page should accept tempFilePath when choosing local files', () => {
  const pagePath = resolve(__dirname, '../pages/competition/submission/index.js')
  let pageConfig = null

  delete require.cache[pagePath]

  global.Page = (config) => {
    pageConfig = config
  }
  global.wx = {
    chooseMessageFile(options) {
      options.success({
        tempFiles: [
          {
            tempFilePath: 'C:/tmp/work.pdf',
            name: 'work.pdf',
            size: 2048
          }
        ]
      })
    }
  }

  require(pagePath)

  const pageInstance = {
    data: {
      ...pageConfig.data
    },
    setData(update) {
      Object.assign(this.data, update)
    }
  }

  pageConfig.chooseLocalFile.call(pageInstance)

  assert.equal(pageInstance.data.selectedFilePath, 'C:/tmp/work.pdf')
  assert.equal(pageInstance.data.selectedFileName, 'work.pdf')
  assert.match(pageInstance.data.selectedFileSummary, /已选择 work\.pdf/)

  delete global.Page
})
