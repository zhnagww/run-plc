package com.runplc.entity;

import lombok.Data;

@Data
public class PlcInfo {
    private Integer id;
    private String ipAddr;
    private Integer portNo;
}