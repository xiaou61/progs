import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('banner page upload', () => {
  it('supports image upload preview and optional jump path', () => {
    const source = readFileSync(resolve(process.cwd(), 'src/views/system/banner/BannerPage.vue'), 'utf-8')

    expect(source).toContain('handleImageChange')
    expect(source).toContain('type="file"')
    expect(source).toContain('accept="image/*"')
    expect(source).toContain('buildAssetUrl(')
    expect(source).toContain('跳转路径（可选）')
  })
})
