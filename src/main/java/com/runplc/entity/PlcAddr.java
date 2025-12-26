package com.runplc.entity;

import lombok.Data;

@Data
public class PlcAddr {
    private Integer id;
    private Integer plcNo;
    private String name;
    private Integer type;
    private Integer plcInfoId;
}