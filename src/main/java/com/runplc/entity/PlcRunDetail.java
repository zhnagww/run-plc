package com.runplc.entity;

import lombok.Data;

@Data
public class PlcRunDetail {
    private Integer id;
    private Integer plcRunId;
    private Integer plcAddrId;
    private Integer sortNo;
    private Integer plcValue = 0;        // 点位值，默认值为0
    private Integer timeOutSecond = 60;  // 超时时间(秒)，默认值为60
}