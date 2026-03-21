import { request, requestMultipartFile } from './http'

export type BannerItem = {
  id: number
  title: string
  status: string
  jumpPath: string
  imageUrl: string | null
}

export type UpdateBannerPayload = {
  title: string
  status: string
  jumpPath: string
  imageUrl: string | null
}

export type BannerUploadResult = {
  originalFileName: string
  imageUrl: string
  fileSize: number
}

export type SystemConfigSummary = {
  platformName: string
  mvpPhase: string
  pointsEnabled: boolean
  submissionReuploadEnabled: boolean
}

export type UpdateSystemConfigPayload = SystemConfigSummary

export async function fetchBanners(): Promise<BannerItem[]> {
  return request<BannerItem[]>('/api/admin/banners')
}

export async function fetchSystemConfig(): Promise<SystemConfigSummary> {
  return request<SystemConfigSummary>('/api/admin/configs')
}

export async function updateBanner(bannerId: number, payload: UpdateBannerPayload): Promise<BannerItem> {
  return request<BannerItem>(`/api/admin/banners/${bannerId}`, {
    method: 'PUT',
    body: payload
  })
}

export async function uploadBannerImage(file: File): Promise<BannerUploadResult> {
  return requestMultipartFile<BannerUploadResult>('/api/admin/banner-files', file)
}

export async function updateSystemConfig(payload: UpdateSystemConfigPayload): Promise<SystemConfigSummary> {
  return request<SystemConfigSummary>('/api/admin/configs', {
    method: 'PUT',
    body: payload
  })
}
