package com.runplc.modbus;

import java.util.Map;

public class SysExectingResult {

    private boolean result;

    private String msg;

    private Map data;

    private Integer code;

    public SysExectingResult() {
        result = true;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


    public static SysExectingResult FAIL(Integer code, String msg, Map data) {

        return new SysExectingResult(false, msg, data, code);
    }


    public SysExectingResult(boolean result, String msg, Map data, Integer code) {
        this.result = result;
        this.msg = msg;
        this.data = data;
        this.code = code;
    }

    @Override
    public String toString() {
        return "SysExectingResult{" +
                "result=" + result +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", code=" + code +
                '}';
    }
}
