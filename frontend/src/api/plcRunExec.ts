import { http, request } from './http'

export const plcRunExecApi = {
  stop: (runId: string) => request<void>(http.post('/api/plc-run-exec/stop', null, { params: { runId } }))
}
