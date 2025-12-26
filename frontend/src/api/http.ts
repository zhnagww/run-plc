import axios from 'axios'
import type { AjaxResult } from '../types'
import { ElMessage } from 'element-plus'

export const http = axios.create({
  baseURL: '',
  timeout: 15000
})

export async function request<T>(p: Promise<{ data: AjaxResult<T> }>): Promise<T> {
  const { data } = await p
  if (data.code === 200) {
    return (data.data as T) ?? (undefined as unknown as T)
  }
  ElMessage.error(data.msg || '请求失败')
  throw new Error(data.msg || '请求失败')
}
