package com.runplc.modbus;


import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@Data
public class PlcService {

    private static int DEFAULT_RETRY_TIMES = 3;
    private static int SLEEP_TIME_MS = 500;

    @Value("${plc.ip:192.168.10.95}")
    private String ip;
    @Value("${plc.port:502}")
    private Integer port;

    public int readPLC(int address) {
        return readPLC(getIp(), getPort(), address);
    }

    public boolean writePLC(int address, short value) {
        return writePLC(getIp(), getPort(), address, value);
    }

    /**
     * @param writeAddress  写入地址
     * @param writeValue    写入值
     * @param feedbackAddr  反馈地址
     * @param feedbackValue 反馈值
     * @return boolean
     * @Description: PLC写指令并校验写结果_默认重试3次
     * @Date: 2024/8/16 9:54
     * @Author: sh
     */
    public boolean writePLCAndValidate(int writeAddress, short writeValue, int feedbackAddr, short feedbackValue) {
        return writePLCAndValidate(writeAddress, writeValue, feedbackAddr, feedbackValue, DEFAULT_RETRY_TIMES);
    }

    /**
     * @param writeAddress  写入地址
     * @param writeValue    写入值
     * @param feedbackAddr  反馈地址
     * @param feedbackValue 反馈值
     * @return boolean
     * @Description: PLC写指令并校验写结果_默认重试3次
     * @Date: 2024/8/16 9:54
     * @Author: sh
     */
    public boolean writePLCAndValidate(String ip, int port, int writeAddress, short writeValue, int feedbackAddr, short feedbackValue) {
        return writePLCAndValidate(ip, port, writeAddress, writeValue, feedbackAddr, feedbackValue, DEFAULT_RETRY_TIMES);
    }

    /**
     * @param writeAddress  写入地址
     * @param writeValue    写入值
     * @param feedbackAddr  反馈地址
     * @param feedbackValue 反馈值
     * @param retryTimes    重试次数
     * @return boolean
     * @Description: PLC写指令并校验写结果_传入重试次数
     * @Date: 2024/8/16 9:54
     * @Author: sh
     */
    public boolean writePLCAndValidate(int writeAddress, short writeValue, int feedbackAddr, short feedbackValue, int retryTimes) {
        try {
            ModbusMaster master = ModbusUtils.getTcpIpMaster(getIp(), getPort());
            ModbusUtils.writeHoldingRegister(master, 1, writeAddress, writeValue);
            while (true) {
                for (int i = 0; i < retryTimes; i++) {
                    Thread.sleep(SLEEP_TIME_MS);
                    int retValue = this.readPLC(feedbackAddr);
                    if (feedbackValue == retValue) {
                        return true;
                    } else if (2 == retValue) {
                        return false;
                    }
                }
                //       return false;
            }
        } catch (Exception e) {
            log.error("写入点位" + writeAddress + "异常：{}", e.getMessage());
            return false;
        }
    }

    /**
     * @param writeAddress  写入地址
     * @param writeValue    写入值
     * @param feedbackAddr  反馈地址
     * @param feedbackValue 反馈值
     * @param retryTimes    重试次数
     * @return boolean
     * @Description: PLC写指令并校验写结果_传入重试次数
     * @Date: 2024/8/16 9:54
     * @Author: sh
     */
    public boolean writePLCAndValidate(String ip, int port, int writeAddress, short writeValue, int feedbackAddr, short feedbackValue, int retryTimes) {
        int x = 0;
        while (true) {
            try {
                for (int i = 0; i < retryTimes; i++) {
                    ModbusMaster master = ModbusUtils.getTcpIpMaster(ip, port);
                    ModbusUtils.writeHoldingRegister(master, 1, writeAddress, writeValue);
                    Thread.sleep(SLEEP_TIME_MS);
                    int retValue = this.readPLC(ip, port, feedbackAddr);
                    if (feedbackValue == retValue) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("写入点位" + writeAddress + "异常：{}", e.getMessage());
                if (x++ >= DEFAULT_RETRY_TIMES) {
                    return false;
                }
                threadSleep(SLEEP_TIME_MS);
                continue;
            }
        }
    }

    public SysExectingResult writePlcAndValidate(String ip,Integer port,int writeAddress, short writeValue, int feedbackAddr, short feedbackValue, int timeoutSecond) {
        SysExectingResult result = new SysExectingResult();
        boolean plcWrite = writePLC(ip,port,writeAddress, writeValue);
        if (plcWrite==false){
            result.setResult(false);
            result.setMsg("PLC写入点位异常，点位："+ writeAddress+" 值："+writeValue);
            return result;
        }
        int readPLC = 0;
        for (int i=0;i<timeoutSecond;i++){
            readPLC = readPLC(ip,port,feedbackAddr);
            if (readPLC!=0){
                break;
            }
            threadSleep(50);
        }
        if (readPLC==0){
            result.setResult(false);
            result.setMsg("PLC读取点位超时，点位："+ feedbackAddr);
        }else {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("readPLCValue",readPLC);
            result.setData(map);
            result.setResult(true);
        }
        return result;
    }


    public SysExectingResult plcLister(String ip,Integer port,Integer plcAddr, Integer targetValue ,Integer timeoutSecond){
        threadSleep(1000);
        SysExectingResult result = new SysExectingResult();
        int readPLC = 0;
        for (int i = 0; i < timeoutSecond; i++) {
            threadSleep(1 * 1000);
            readPLC = readPLC(ip,port,plcAddr);
            if (readPLC == targetValue.intValue()) {
                break;
            }
        }
        if (readPLC==0){
            result.setResult(false);
            result.setMsg("PLC读取点位超时，点位："+ plcAddr);
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("readPLCValue",readPLC);
        result.setData(map);
        return result;
    }
    /**
     * @param addrs
     * @return void
     * @Description: 地址清零
     * @Date: 2024/8/16 13:40
     * @Author: sh
     */
    public boolean clear(int... addrs) {
        for (int addr : addrs) {
            boolean result = this.writePLC(addr, (short) 0);
            if (!result) {

                return false;
            }
        }
        return true;
    }

    public int readPLC(String ip, int port, int address) {
        try {
            ModbusMaster master = ModbusUtils.getTcpIpMaster(ip, port);
            int value = ModbusUtils.readHoldingRegister(master, 1, address, 2).intValue();
            return value;
        } catch (Exception e) {
            log.error("点位读取" + address + "异常：{}", e.getMessage());
            return 9999;
        }
    }

    public boolean readCoilStatus(String ip, int port, int address){
        try {
            ModbusMaster master = ModbusUtils.getTcpIpMaster(ip, port);
            master.setTimeout(5000);
            assert master != null;
            boolean booleanValue = ModbusUtils.readCoilStatus(master, 1, address).booleanValue();
            return booleanValue;
        }catch (Exception e){
            log.error("线圈点位读取" + address + "异常：{}", e.getMessage());
        }
        return false;
    }

    public boolean writeCoilStatus(String ip, int port, int address,boolean writeValue){
        try {
            ModbusMaster master = ModbusUtils.getTcpIpMaster(ip, port);
            threadSleep(3000);
            assert master != null;
            ModbusUtils.writeCoil(master, 1, address,writeValue);
            return true;
        }catch (Exception e){
            log.error("线圈点位读取" + address + "异常：{}", e.getMessage());
        }
        return false;
    }

    /**
     * 持续读plc 返回结果是1或2  或自定义参数
     *
     * @param feedbackAddr
     * @param feedbackValue 等于null 则只判断1或2 反之 判断传入参数
     * @param retryTimes
     * @return
     */
    public boolean readPLCExecResult(int feedbackAddr, Integer feedbackValue, int retryTimes) {
        int x = 0;
        while (true) {
            try {
                for (int i = 0; i < retryTimes; i++) {
                    Thread.sleep(SLEEP_TIME_MS);
                    int retValue = this.readPLC(feedbackAddr);
                    if (feedbackValue == null) {
                        if (1 == retValue) {
                            return true;
                        } else if (2 == retValue) {
                            return false;
                        }
                    } else {
                        if (feedbackValue == retValue) {
                            return true;
                        }
                    }

                }
                //       return false;
            } catch (Exception e) {
                log.error("写入点位" + feedbackAddr + "异常：{}", e.getMessage());
                if (x++ >= DEFAULT_RETRY_TIMES) {
                    return false;
                }
                threadSleep(SLEEP_TIME_MS);
                continue;
            }
        }
    }

    public boolean writePLC(String ip, int port, int address, short value) {
        int x = 0;
        while (true) {
            try {
                ModbusMaster master = ModbusUtils.getTcpIpMaster(ip, port);
                ModbusUtils.writeHoldingRegister(master, 1, address, value);
                return true;
            } catch (Exception e) {
                log.error("写入点位" + address + "异常：{}", e.getMessage());
                if (x++ >= DEFAULT_RETRY_TIMES) {
                    return false;
                }
                threadSleep(200);
                continue;
            }
        }
    }
    public void threadSleep(long millis){
        try {
            Thread.sleep(millis);
        }catch (Exception e){
            log.error("线程休眠异常~");
        }
    }
    public SysExectingResult plcClear0(String ip,Integer port,String... points) {
        SysExectingResult result = new SysExectingResult();
        for (String str:points){
            boolean writePLC12 = writePLC(ip,port,Integer.parseInt(str), Short.valueOf("0"));
            if (writePLC12==false){
                result.setResult(false);
                result.setMsg("PLC点位清0失败，点位："+ str);
                return result;
            }
            threadSleep(50);
        }
        return result;
    }
    public static void main(String[] args) throws ModbusInitException, ModbusTransportException, ErrorResponseException {
        try {
            ModbusMaster master = ModbusUtils.getTcpIpMaster("10.101.47.134", 502);
            assert master != null;
            ModbusUtils.writeCoil(master, 1, 16,true);
        }catch (Exception e){
            log.error("线圈点位读取" + 1 + "异常：{}", e.getMessage());
        }
    }
}
