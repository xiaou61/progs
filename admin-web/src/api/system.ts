import { request } from './http'

export type BannerItem = {
  id: number
  title: string
  status: string
  jumpPath: string
}

export type SystemConfigSummary = {
  platformName: string
  mvpPhase: string
  pointsEnabled: boolean
  submissionReuploadEnabled: boolean
}

export async function fetchBanners(): Promise<BannerItem[]> {
  return request<BannerItem[]>('/api/admin/banners')
}

export async function fetchSystemConfig(): Promise<SystemConfigSummary> {
  return request<SystemConfigSummary>('/api/admin/configs')
}
