const {
  fetchTeacherManagedCompetitions,
  offlineTeacherCompetition,
  publishTeacherCompetition,
  saveTeacherCompetitionDraft,
  updateTeacherCompetition,
  updateTeacherCompetitionFeature
} = require('../../../services/competition')
const {
  buildDefaultPickerValue,
  formatPickerDateTimeLabel,
  mergeDateTimeValue,
  splitDateTimeValue
} = require('../../../utils/datetime')
const { getRoleCode, getSession, requireLogin } = require('../../../utils/auth')

const TIME_FIELDS = ['signupStartAt', 'signupEndAt', 'startAt', 'endAt']

function buildEmptyForm() {
  return {
    title: '',
    description: '',
    signupStartAt: '',
    signupEndAt: '',
    startAt: '',
    endAt: '',
    quota: 120,
    participantType: 'TEACHER_ONLY'
  }
}

function buildFeatureForm() {
  return {
    recommended: false,
    pinned: false
  }
}

function buildCompetitionForm(item) {
  if (!item) {
    return buildEmptyForm()
  }
  return {
    title: item.title,
    description: item.description,
    signupStartAt: item.signupStartAt,
    signupEndAt: item.signupEndAt,
    startAt: item.startAt,
    endAt: item.endAt,
    quota: item.quota,
    participantType: item.participantType || 'TEACHER_ONLY'
  }
}

function buildTimePickerState(form) {
  return TIME_FIELDS.reduce((state, field) => {
    const parts = splitDateTimeValue(form[field])
    state[field] = {
      date: parts.date,
      time: parts.time,
      label: formatPickerDateTimeLabel(parts.date, parts.time)
    }
    return state
  }, {})
}

function resolveParticipantTypeLabel(participantType) {
  return participantType === 'STUDENT_ONLY' ? '仅学生参加' : '仅老师参加'
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
    pickerDefaults: buildDefaultPickerValue(),
    timePickerState: buildTimePickerState(buildEmptyForm()),
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
      timePickerState: buildTimePickerState(buildEmptyForm()),
      featureForm: buildFeatureForm(),
      selectedCompetitionId: null,
      error: '',
      success: ''
    })
  },

  handleDateChange(event) {
    this.updateDateTimeField(event.currentTarget.dataset.field, {
      date: event.detail.value
    })
  },

  handleTimeChange(event) {
    this.updateDateTimeField(event.currentTarget.dataset.field, {
      time: event.detail.value
    })
  },

  updateDateTimeField(field, patch) {
    const pickerDefaults = this.data.pickerDefaults || buildDefaultPickerValue()
    const currentParts = this.data.timePickerState[field] || {
      date: '',
      time: '',
      label: formatPickerDateTimeLabel('', '')
    }
    const nextDate = patch.date || currentParts.date || pickerDefaults.date
    const nextTime = patch.time || currentParts.time || pickerDefaults.time
    const nextValue = mergeDateTimeValue(nextDate, nextTime)

    this.setData({
      [`form.${field}`]: nextValue,
      [`timePickerState.${field}`]: {
        date: nextDate,
        time: nextTime,
        label: formatPickerDateTimeLabel(nextDate, nextTime)
      }
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
      form: buildCompetitionForm(item),
      timePickerState: buildTimePickerState(buildCompetitionForm(item)),
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
      const myCompetitions = await fetchTeacherManagedCompetitions(session.userId)
      this.setData({
        competitions: myCompetitions,
        myCompetitions
      })

      const targetId = preferId || this.data.selectedCompetitionId
      if (!targetId) {
        return
      }

      const target = myCompetitions.find((item) => item.id === targetId)
      if (target) {
        const form = buildCompetitionForm(target)
        this.setData({
          selectedCompetitionId: target.id,
          form,
          timePickerState: buildTimePickerState(form),
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
    if (!form.participantType) {
      return '请选择参赛类型'
    }
    return ''
  },

  buildPayload() {
    const session = getSession()
    const participantType = this.data.form.participantType || 'TEACHER_ONLY'
    return {
      organizerId: session.userId,
      title: this.data.form.title.trim(),
      description: this.data.form.description.trim(),
      signupStartAt: this.data.form.signupStartAt.trim(),
      signupEndAt: this.data.form.signupEndAt.trim(),
      startAt: this.data.form.startAt.trim(),
      endAt: this.data.form.endAt.trim(),
      quota: Number(this.data.form.quota),
      participantType,
      advisorTeacherId: participantType === 'STUDENT_ONLY' ? session.userId : null,
      recommended: this.data.featureForm.recommended,
      pinned: this.data.featureForm.pinned
    }
  },

  resolveParticipantTypeLabel,

  async submitPublish() {
    const session = getSession()
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
        await updateTeacherCompetition(session.userId, this.data.selectedCompetitionId, {
          ...this.buildPayload(),
          status: 'PUBLISHED'
        })
        this.setData({
          success: `比赛 #${this.data.selectedCompetitionId} 更新成功`
        })
        await this.loadCompetitions(this.data.selectedCompetitionId)
      } else {
        const competitionId = await publishTeacherCompetition(session.userId, this.buildPayload())
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
    const session = getSession()
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
        await updateTeacherCompetition(session.userId, this.data.selectedCompetitionId, {
          ...this.buildPayload(),
          status: 'DRAFT'
        })
        this.setData({
          success: `草稿更新成功，编号 #${this.data.selectedCompetitionId}`
        })
        await this.loadCompetitions(this.data.selectedCompetitionId)
      } else {
        const competitionId = await saveTeacherCompetitionDraft(session.userId, this.buildPayload())
        this.setData({
          success: `草稿保存成功，编号 #${competitionId}`
        })
        await this.loadCompetitions(competitionId)
      }
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '保存草稿失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async submitFeature() {
    const session = getSession()
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
      await updateTeacherCompetitionFeature(session.userId, this.data.selectedCompetitionId, this.data.featureForm)
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
    const session = getSession()
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
      await offlineTeacherCompetition(session.userId, this.data.selectedCompetitionId)
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
