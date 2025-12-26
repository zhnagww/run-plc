import { http } from './http'
import type { AjaxResult, PlcImportReport } from '../types'

export const plcImportApi = {
  importExcel: async (file: File, strict: boolean): Promise<AjaxResult<PlcImportReport>> => {
    const form = new FormData()
    form.append('file', file)
    const { data } = await http.post<AjaxResult<PlcImportReport>>('/api/plc-import/excel', form, {
      params: { strict },
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return data
  }
}
