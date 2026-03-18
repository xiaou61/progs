const {
  fetchManagedCompetitions,
  offlineCompetition,
  publishCompetition,
  saveCompetitionDraft,
  updateCompetition,
  updateCompetitionFeature
} = require('../../../services/competition')
const { getRoleCode, getSession, requireLogin } = require('../../../utils/auth')

function buildEmptyForm() {
  return {
    title: '',
    description: '',
    signupStartAt: '',
    signupEndAt: '',
    startAt: '',
    endAt: '',
    quota: 120
  }
}

function buildFeatureForm() {
  return {
    recommended: false,
    pinned: false
  }
}

Page({
  data: {
    loading: false,
    listLoading: false,
    featureLoading: false,
    error: '',
    success: '',
    selectedCompetitionId: null,
    form: buildEmptyForm(),
    featureForm: buildFeatureForm(),
    competitions: [],
    myCompetitions: []
  },

  onShow() {
    if (!requireLogin('/pages/teacher/competition-editor/index')) {
      return
    }
    if (getRoleCode() !== 'TEACHER') {
      this.setData({
        error: '当前账号不是老师，暂不支持管理比赛'
      })
      return
    }
    this.loadCompetitions()
  },

  updateField(event) {
    const field = event.currentTarget.dataset.field
    const value = field === 'form.quota' ? Number(event.detail.value || 0) : event.detail.value
    this.setData({
      [field]: value
    })
  },

  updateSwitch(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [field]: event.detail.value
    })
  },

  resetForm() {
    this.setData({
      form: buildEmptyForm(),
      featureForm: buildFeatureForm(),
      selectedCompetitionId: null,
      error: '',
      success: ''
    })
  },

  applyCompetitionById(event) {
    const competitionId = Number(event.currentTarget.dataset.id)
    const item = this.data.myCompetitions.find((current) => current.id === competitionId)
    if (!item) {
      return
    }

    this.setData({
      selectedCompetitionId: item.id,
      form: {
        title: item.title,
        description: item.description,
        signupStartAt: item.signupStartAt,
        signupEndAt: item.signupEndAt,
        startAt: item.startAt,
        endAt: item.endAt,
        quota: item.quota
      },
      featureForm: {
        recommended: Boolean(item.recommended),
        pinned: Boolean(item.pinned)
      },
      error: '',
      success: ''
    })
  },

  async loadCompetitions(preferId) {
    const session = getSession()
    this.setData({
      listLoading: true
    })

    try {
      const competitions = await fetchManagedCompetitions()
      const myCompetitions = competitions.filter((item) => item.organizerId === session.userId)
      this.setData({
        competitions,
        myCompetitions
      })

      const targetId = preferId || this.data.selectedCompetitionId
      if (!targetId) {
        return
      }

      const target = myCompetitions.find((item) => item.id === targetId)
      if (target) {
        this.setData({
          selectedCompetitionId: target.id,
          form: {
            title: target.title,
            description: target.description,
            signupStartAt: target.signupStartAt,
            signupEndAt: target.signupEndAt,
            startAt: target.startAt,
            endAt: target.endAt,
            quota: target.quota
          },
          featureForm: {
            recommended: Boolean(target.recommended),
            pinned: Boolean(target.pinned)
          }
        })
      }
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载比赛管理列表失败'
      })
    } finally {
      this.setData({ listLoading: false })
    }
  },

  validateForm() {
    const { form } = this.data
    if (!form.title.trim()) {
      return '比赛名称不能为空'
    }
    if (!form.description.trim()) {
      return '比赛说明不能为空'
    }
    if (!form.signupStartAt || !form.signupEndAt || !form.startAt || !form.endAt) {
      return '请完整填写比赛时间'
    }
    if (new Date(form.signupEndAt).getTime() < new Date(form.signupStartAt).getTime()) {
      return '报名截止时间不能早于报名开始时间'
    }
    if (new Date(form.endAt).getTime() < new Date(form.startAt).getTime()) {
      return '比赛结束时间不能早于比赛开始时间'
    }
    if (!form.quota || form.quota <= 0) {
      return '比赛名额必须大于 0'
    }
    return ''
  },

  buildPayload() {
    const session = getSession()
    return {
      organizerId: session.userId,
      title: this.data.form.title.trim(),
      description: this.data.form.description.trim(),
      signupStartAt: this.data.form.signupStartAt.trim(),
      signupEndAt: this.data.form.signupEndAt.trim(),
      startAt: this.data.form.startAt.trim(),
      endAt: this.data.form.endAt.trim(),
      quota: Number(this.data.form.quota)
    }
  },

  async submitPublish() {
    const validationMessage = this.validateForm()
    if (validationMessage) {
      this.setData({ error: validationMessage, success: '' })
      return
    }

    this.setData({
      loading: true,
      error: '',
      success: ''
    })

    try {
      if (this.data.selectedCompetitionId) {
        await updateCompetition(this.data.selectedCompetitionId, {
          ...this.buildPayload(),
          status: 'PUBLISHED'
        })
        this.setData({
          success: `比赛 #${this.data.selectedCompetitionId} 更新成功`
        })
        await this.loadCompetitions(this.data.selectedCompetitionId)
      } else {
        const competitionId = await publishCompetition(this.buildPayload())
        this.setData({
          success: `比赛发布成功，编号 #${competitionId}`
        })
        await this.loadCompetitions(competitionId)
      }
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '发布或更新比赛失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async submitDraft() {
    const validationMessage = this.validateForm()
    if (validationMessage) {
      this.setData({ error: validationMessage, success: '' })
      return
    }

    this.setData({
      loading: true,
      error: '',
      success: ''
    })

    try {
      const competitionId = await saveCompetitionDraft(this.buildPayload())
      this.setData({
        success: `草稿保存成功，编号 #${competitionId}`
      })
      await this.loadCompetitions(competitionId)
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '保存草稿失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async submitFeature() {
    if (!this.data.selectedCompetitionId) {
      this.setData({ error: '请先选择一个比赛，再设置推荐和置顶', success: '' })
      return
    }

    this.setData({
      featureLoading: true,
      error: '',
      success: ''
    })

    try {
      await updateCompetitionFeature(this.data.selectedCompetitionId, this.data.featureForm)
      this.setData({
        success: `比赛 #${this.data.selectedCompetitionId} 的展示状态已更新`
      })
      await this.loadCompetitions(this.data.selectedCompetitionId)
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '更新推荐置顶失败'
      })
    } finally {
      this.setData({ featureLoading: false })
    }
  },

  async submitOffline() {
    if (!this.data.selectedCompetitionId) {
      this.setData({ error: '请先选择一个比赛，再执行下架', success: '' })
      return
    }

    this.setData({
      featureLoading: true,
      error: '',
      success: ''
    })

    try {
      await offlineCompetition(this.data.selectedCompetitionId)
      this.setData({
        success: `比赛 #${this.data.selectedCompetitionId} 已下架`
      })
      await this.loadCompetitions(this.data.selectedCompetitionId)
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '下架比赛失败'
      })
    } finally {
      this.setData({ featureLoading: false })
    }
  }
})
