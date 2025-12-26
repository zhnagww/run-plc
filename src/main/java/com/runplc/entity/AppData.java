package com.runplc.entity;

import lombok.Data;
import java.util.Date;

@Data
public class AppData {
    private Integer id;
    private String dataKey;
    private String dataValue;
    private String dataType;
    private Date createdAt;
}