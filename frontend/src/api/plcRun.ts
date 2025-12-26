import { http, request } from './http'
import type { PlcRun, PlcRunCreateRequest } from '../types'

export const plcRunApi = {
  list: () => request<PlcRun[]>(http.get('/api/plc-run')),
  get: (id: number) => request<PlcRun>(http.get('/api/plc-run/get', { params: { id } })),
  create: (payload: PlcRunCreateRequest) => request<void>(http.post('/api/plc-run', payload)),
  update: (payload: PlcRun) => request<void>(http.post('/api/plc-run/update', payload)),
  remove: (id: number) => request<void>(http.post('/api/plc-run/delete', null, { params: { id } }))
}
