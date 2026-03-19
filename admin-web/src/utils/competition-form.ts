import type { CompetitionParticipantType } from '@/api/competition'

export type CompetitionFormValues = {
  organizerId: number
  title: string
  description: string
  signupStartAt: string
  signupEndAt: string
  startAt: string
  endAt: string
  quota: number
  participantType: CompetitionParticipantType
  advisorTeacherId: number | null
}

function toComparableTime(value: string) {
  return new Date(value).getTime()
}

function normalizeDateTime(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

export function validateCompetitionForm(form: CompetitionFormValues) {
  if (!form.organizerId || form.organizerId <= 0) {
    return '发起人编号不能为空'
  }
  if (!form.title.trim()) {
    return '比赛名称不能为空'
  }
  if (!form.description.trim()) {
    return '比赛说明不能为空'
  }
  if (!form.signupStartAt || !form.signupEndAt || !form.startAt || !form.endAt) {
    return '请完整填写比赛时间'
  }
  if (toComparableTime(form.signupEndAt) < toComparableTime(form.signupStartAt)) {
    return '报名截止时间不能早于报名开始时间'
  }
  if (toComparableTime(form.endAt) < toComparableTime(form.startAt)) {
    return '比赛结束时间不能早于比赛开始时间'
  }
  if (!form.quota || form.quota <= 0) {
    return '比赛名额必须大于 0'
  }
  if (form.participantType === 'STUDENT_ONLY' && (!form.advisorTeacherId || form.advisorTeacherId <= 0)) {
    return '学生赛必须指定指导老师'
  }
  return ''
}

export function buildPublishPayload(form: CompetitionFormValues) {
  const advisorTeacherId = form.participantType === 'STUDENT_ONLY' && (form.advisorTeacherId ?? 0) > 0
    ? form.advisorTeacherId
    : null
  return {
    organizerId: form.organizerId,
    title: form.title.trim(),
    description: form.description.trim(),
    signupStartAt: normalizeDateTime(form.signupStartAt),
    signupEndAt: normalizeDateTime(form.signupEndAt),
    startAt: normalizeDateTime(form.startAt),
    endAt: normalizeDateTime(form.endAt),
    quota: form.quota,
    participantType: form.participantType,
    advisorTeacherId
  }
}
