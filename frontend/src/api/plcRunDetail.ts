import { http, request } from './http'
import type { PlcRunDetail } from '../types'

export const plcRunDetailApi = {
  listByPlcRunId: (plcRunId: number) =>
    request<PlcRunDetail[]>(http.get('/api/plc-run-detail/get-by-plc-run-id', { params: { plcRunId } })),
  create: (payload: PlcRunDetail) => request<void>(http.post('/api/plc-run-detail', payload)),
  update: (payload: PlcRunDetail) => request<void>(http.post('/api/plc-run-detail/update', payload)),
  updateSortNo: (id: number, sortNo: number) => request<void>(http.post('/api/plc-run-detail/update-sort-no', { id, sortNo })),
  remove: (id: number) => request<void>(http.post('/api/plc-run-detail/delete', null, { params: { id } }))
}
