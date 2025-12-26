import { http, request } from './http'

export const plcDebugApi = {
  read: (plcAddrId: number) => request<{ value: number }>(http.get('/api/plc-debug/read', { params: { plcAddrId } })),
  write: (plcAddrId: number, value: number) => request<void>(http.post('/api/plc-debug/write', { plcAddrId, value }))
}
