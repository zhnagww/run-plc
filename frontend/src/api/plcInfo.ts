import { http, request } from './http'
import type { PlcInfo } from '../types'

export const plcInfoApi = {
  list: () => request<PlcInfo[]>(http.get('/api/plc-info')),
  get: (id: number) => request<PlcInfo>(http.get('/api/plc-info/get', { params: { id } })),
  create: (payload: PlcInfo) => request<void>(http.post('/api/plc-info', payload)),
  update: (payload: PlcInfo) => request<void>(http.post('/api/plc-info/update', payload)),
  remove: (id: number) => request<void>(http.post('/api/plc-info/delete', null, { params: { id } }))
}
