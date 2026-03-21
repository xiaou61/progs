import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('competition editor feature save behavior', () => {
  it('treats recommended and pinned as part of the main competition save payload', () => {
    const apiSource = readFileSync(resolve(process.cwd(), 'src/api/competition.ts'), 'utf-8')
    const formSource = readFileSync(resolve(process.cwd(), 'src/utils/competition-form.ts'), 'utf-8')
    const editorSource = readFileSync(resolve(process.cwd(), 'src/views/competition/CompetitionEditorPage.vue'), 'utf-8')

    expect(apiSource).toMatch(/PublishCompetitionPayload\s*=\s*\{[\s\S]*recommended:\s*boolean[\s\S]*pinned:\s*boolean/)
    expect(formSource).toMatch(/buildPublishPayload\([\s\S]*featureForm[\s\S]*recommended:\s*featureForm\.recommended/)
    expect(formSource).toMatch(/buildPublishPayload\([\s\S]*featureForm[\s\S]*pinned:\s*featureForm\.pinned/)
    expect(editorSource).toMatch(/buildPublishPayload\(form,\s*featureForm\)/)
  })

  it('auto-selects the first managed competition so feature actions are immediately available', () => {
    const editorSource = readFileSync(resolve(process.cwd(), 'src/views/competition/CompetitionEditorPage.vue'), 'utf-8')

    expect(editorSource).toMatch(/if\s*\(!targetId\)\s*\{[\s\S]*competitions\.value\.length\s*>\s*0[\s\S]*applyCompetition\(competitions\.value\[0\]\)/)
    expect(editorSource).toContain('推荐/置顶和下架针对当前选中的比赛生效')
  })
})
