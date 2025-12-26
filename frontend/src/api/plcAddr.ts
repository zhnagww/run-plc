import { http, request } from './http'
import type { PlcAddr } from '../types'

export const plcAddrApi = {
  listByPlcInfoId: (plcInfoId: number) =>
    request<PlcAddr[]>(http.get('/api/plc-addr/get-by-plc-info-id', { params: { plcInfoId } })),
  create: (payload: PlcAddr) => request<void>(http.post('/api/plc-addr', payload)),
  update: (payload: PlcAddr) => request<void>(http.post('/api/plc-addr/update', payload)),
  remove: (id: number) => request<void>(http.post('/api/plc-addr/delete', null, { params: { id } }))
}
