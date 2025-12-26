export type AjaxResult<T = unknown> = {
  code: number
  msg: string
  data?: T
}

export type PlcInfo = {
  id?: number
  ipAddr: string
  portNo: number
}

export type PlcAddr = {
  id?: number
  plcNo: number
  name: string
  type: 1 | 2
  plcInfoId: number
}

export type PlcRun = {
  id?: number
  name: string
}

export type PlcRunCreateRequest = {
  name: string
  plcAddrIds?: string
}

export type PlcRunDetail = {
  id?: number
  plcRunId: number
  plcAddrId: number
  sortNo: number
  plcValue: number
  timeOutSecond: number
}

export type PlcImportError = {
  row: number
  message: string
}

export type PlcImportReport = {
  plcInserted: number
  plcSkipped: number
  addrInserted: number
  addrSkipped: number
  errors: PlcImportError[]
}
