const { fetchCompetitions } = require('../../../services/competition')
const {
  fetchCompetitionSubmissions,
  submitCompetitionWork,
  uploadCompetitionWorkFile
} = require('../../../services/submission')
const { formatCompetitionDateTime, resolveCompetitionId } = require('../../../utils/competition')
const { getSession, requireLogin } = require('../../../utils/auth')

function buildVersionCards(items, userId) {
  return items
    .filter((item) => item.userId === userId)
    .sort((left, right) => right.versionNo - left.versionNo)
    .map((item, index) => ({
      id: item.id,
      label: `v${item.versionNo}`,
      fileName: resolveWorkFileName(item.fileUrl),
      fileUrl: item.fileUrl,
      status: index === 0 ? '当前版本' : '历史版本',
      submittedAt: formatCompetitionDateTime(item.submittedAt)
    }))
}

function resolveWorkFileName(fileUrl) {
  if (!fileUrl) {
    return '未命名文件'
  }
  const segments = String(fileUrl).split('/')
  return segments[segments.length - 1] || String(fileUrl)
}

function buildSelectedFileSummary(fileName, sizeLabel) {
  if (!fileName) {
    return '还没有选择作品文件'
  }
  return sizeLabel ? `已选择 ${fileName}，大小 ${sizeLabel}` : `已选择 ${fileName}`
}

function buildSelectedFileState(filePath, fileName, size) {
  const sizeLabel = formatFileSize(size)
  return {
    selectedFilePath: filePath || '',
    selectedFileName: fileName || '',
    selectedFileSizeLabel: sizeLabel,
    selectedFileSummary: buildSelectedFileSummary(fileName || '', sizeLabel)
  }
}

function resolveChosenFile(result) {
  const tempFile = result && Array.isArray(result.tempFiles) ? result.tempFiles[0] : null
  const tempFilePath = tempFile && (
    tempFile.path ||
    tempFile.tempFilePath ||
    tempFile.filePath
  )
  const fallbackPath = result && Array.isArray(result.tempFilePaths) ? result.tempFilePaths[0] : ''
  const filePath = tempFilePath || fallbackPath || ''

  if (!tempFile && !filePath) {
    return null
  }

  const fileName = (tempFile && tempFile.name) || resolveWorkFileName(filePath) || '未命名文件'
  const size = tempFile && tempFile.size ? tempFile.size : 0

  return {
    filePath,
    fileName,
    size
  }
}

Page({
  data: {
    competitionId: 0,
    competitionTitle: '未选择比赛',
    competitionDesc: '支持重新上传并保留版本号，最终以最新版本作为评审依据。',
    fileUrl: '',
    selectedFilePath: '',
    selectedFileName: '',
    selectedFileSizeLabel: '',
    selectedFileSummary: '还没有选择作品文件',
    reuploadAllowed: false,
    loading: false,
    submitting: false,
    error: '',
    success: '',
    versionCards: []
  },

  onLoad(options) {
    this.setData({
      competitionId: resolveCompetitionId(options)
    })
  },

  onShow() {
    if (!this.data.competitionId) {
      this.setData({
        error: '缺少比赛编号，请从比赛列表重新进入。'
      })
      return
    }
    const redirectUrl = `/pages/competition/submission/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }
    this.loadSubmissionState()
  },

  updateField(event) {
    const field = event.currentTarget.dataset.field
    const value = event.detail.value
    this.setData({
      [field]: value
    })
  },

  async loadSubmissionState() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [competitions, submissionItems] = await Promise.all([
        fetchCompetitions(),
        fetchCompetitionSubmissions(this.data.competitionId)
      ])
      const currentCompetition = competitions.find((item) => item.id === this.data.competitionId)
      const versionCards = buildVersionCards(submissionItems, session.userId)
      this.setData({
        competitionTitle: currentCompetition ? currentCompetition.title : this.data.competitionTitle,
        competitionDesc: currentCompetition ? currentCompetition.description : this.data.competitionDesc,
        versionCards,
        fileUrl: versionCards.length > 0 ? versionCards[0].fileUrl : ''
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载作品记录失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  chooseLocalFile() {
    if (typeof wx === 'undefined' || typeof wx.chooseMessageFile !== 'function') {
      this.setData({
        error: '当前环境不支持本地文件选择'
      })
      return
    }

    this.setData({
      error: '',
      success: ''
    })

    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success: (result) => {
        const file = resolveChosenFile(result)
        if (!file) {
          return
        }
        this.setData(buildSelectedFileState(file.filePath, file.fileName, file.size))
      },
      fail: (error) => {
        if (error && typeof error.errMsg === 'string' && error.errMsg.includes('cancel')) {
          return
        }
        this.setData({
          error: '选择文件失败，请重试'
        })
      }
    })
  },

  async submitWork() {
    const session = getSession()
    this.setData({
      submitting: true,
      error: '',
      success: ''
    })

    if (!this.data.selectedFilePath) {
      this.setData({
        submitting: false,
        error: '请先选择本地文件'
      })
      return
    }

    try {
      const uploadResult = await uploadCompetitionWorkFile({
        filePath: this.data.selectedFilePath,
        name: this.data.selectedFileName
      })
      const submissionId = await submitCompetitionWork({
        competitionId: this.data.competitionId,
        userId: session.userId,
        fileUrl: uploadResult.fileUrl,
        reuploadAllowed: this.data.reuploadAllowed
      })
      this.setData({
        success: `作品提交成功，记录编号 #${submissionId}`,
        fileUrl: uploadResult.fileUrl,
        ...buildSelectedFileState('', '', 0)
      })
      await this.loadSubmissionState()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '作品提交失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})

function formatFileSize(size) {
  if (!size || size <= 0) {
    return ''
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}
