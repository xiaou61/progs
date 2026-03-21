type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
}

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
}

import { loadAdminSession } from '@/stores/admin-session'

function buildUrl(path: string) {
  const baseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')
  return `${baseUrl}${path}`
}

export function buildAssetUrl(path: string) {
  if (!path) {
    return ''
  }
  if (/^https?:\/\//i.test(path)) {
    return path
  }
  const baseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')
  return baseUrl ? `${baseUrl}${path}` : path
}

export async function requestMultipartFile<T>(path: string, file: File, fieldName = 'file'): Promise<T> {
  const session = loadAdminSession()
  const headers: Record<string, string> = {}
  if (session?.token) {
    headers.Authorization = `Bearer ${session.token}`
  }

  const formData = new FormData()
  formData.append(fieldName, file)

  const response = await fetch(buildUrl(path), {
    method: 'POST',
    headers,
    body: formData
  })
  if (!response.ok) {
    throw new Error(`请求失败：${response.status}`)
  }

  const payload = (await response.json()) as ApiEnvelope<T>
  if (payload.code !== 0) {
    throw new Error(payload.message || '请求失败')
  }

  return payload.data
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const session = loadAdminSession()
  const headers: Record<string, string> = {}
  const init: RequestInit = {
    method: options.method ?? 'GET'
  }

  if (session?.token) {
    headers.Authorization = `Bearer ${session.token}`
  }

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(options.body)
  }

  if (Object.keys(headers).length > 0) {
    init.headers = headers
  }

  const response = await fetch(buildUrl(path), init)
  if (!response.ok) {
    throw new Error(`请求失败：${response.status}`)
  }

  const payload = (await response.json()) as ApiEnvelope<T>
  if (payload.code !== 0) {
    throw new Error(payload.message || '请求失败')
  }

  return payload.data
}

export async function requestText(path: string, options: RequestOptions = {}): Promise<string> {
  const session = loadAdminSession()
  const headers: Record<string, string> = {}
  const init: RequestInit = {
    method: options.method ?? 'GET'
  }

  if (session?.token) {
    headers.Authorization = `Bearer ${session.token}`
  }

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(options.body)
  }

  if (Object.keys(headers).length > 0) {
    init.headers = headers
  }

  const response = await fetch(buildUrl(path), init)
  if (!response.ok) {
    throw new Error(`请求失败：${response.status}`)
  }

  return response.text()
}
