export type PublishResultForm = {
  competitionId: number
  studentId: number
  score: number
  rank: number
  awardName: string
  points: number
}

export function validatePublishResultForm(form: PublishResultForm) {
  if (!form.competitionId || form.competitionId <= 0) {
    return '比赛编号不能为空'
  }
  if (!form.studentId || form.studentId <= 0) {
    return '学生编号不能为空'
  }
  if (form.score < 0) {
    return '成绩不能为负数'
  }
  if (!form.rank || form.rank <= 0) {
    return '名次必须大于 0'
  }
  if (!form.awardName.trim()) {
    return '奖项不能为空'
  }
  if (!form.points || form.points <= 0) {
    return '积分必须大于 0'
  }
  return ''
}
