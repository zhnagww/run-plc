package com.runplc.modbus;

import com.runplc.modbus.serialPort.SerialPortWrapperImpl;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.msg.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ModbusUtils {
    private static Logger log = LoggerFactory.getLogger(ModbusUtils.class);
    /**
     * 工厂。
     */
    static ModbusFactory modbusFactory;
    static HashMap<String,ModbusMaster> masterMap = new HashMap<>();

    static {
        if (modbusFactory == null) {
            modbusFactory = new ModbusFactory();
        }
    }

    /**
     * 获取master
     *
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getTcpIpMaster(String host, int port) throws ModbusInitException {
        ModbusMaster modbusMaster = masterMap.get(host+port);
        if(modbusMaster != null){
            return modbusMaster;
        }
        IpParameters params = new IpParameters();
        params.setHost(host);
        params.setPort(port);
        // modbusFactory.createRtuMaster(wapper); //RTU 协议
        // modbusFactory.createUdpMaster(params);//UDP 协议
        // modbusFactory.createAsciiMaster(wrapper);//ASCII 协议
        try {
            ModbusMaster master = modbusFactory.createTcpMaster(params, true);// TCP 协议
            master.init();

            masterMap.put(host+port, master);
            return master;
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return null;
    }

    public static ModbusMaster getRtuIpMaster(String host, int port) throws ModbusInitException {
        ModbusMaster modbusMaster = masterMap.get(host+port);
        if(modbusMaster != null){
            return modbusMaster;
        }
        IpParameters params = new IpParameters();
        params.setHost(host);
        params.setPort(port);
        params.setEncapsulated(true);
        ModbusMaster master = modbusFactory.createTcpMaster(params, false);
        try {
            //设置超时时间
            master.setTimeout(1000);
            //设置重连次数
            master.setRetries(3);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
        masterMap.put(host+port, master);
        return master;
    }

    /**
     *
     * @param portName 串口名
     * @param baudRate 波特率
     * @param dataBits 数据位
     * @param stopBits 中止位
     * @param parity   校验位
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getSerialPortRtuMaster(String portName, Integer baudRate, Integer dataBits,
                                                      Integer stopBits, Integer parity){

        ModbusMaster modbusMaster = masterMap.get(portName+baudRate+dataBits+stopBits+parity);
        if(modbusMaster != null){
            return modbusMaster;
        }

        try {
        // 设置串口参数，串口是COM1，波特率是9600
        // SerialPortWrapperImpl wrapper = new SerialPortWrapperImpl("COM2", 9600,SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, 0, 0);
        SerialPortWrapperImpl wrapper = new SerialPortWrapperImpl(portName, baudRate,
                dataBits, stopBits, parity, 0, 0);
            modbusMaster = modbusFactory.createRtuMaster(wrapper);

            //设置超时时间
            modbusMaster.setTimeout(1000);
            //设置重连次数
            modbusMaster.setRetries(3);
            //初始化
            modbusMaster.init();


            masterMap.put(portName+baudRate+dataBits+stopBits+parity, modbusMaster);
        } catch (ModbusInitException e) {
           /* log.error("串口连接异常~");
            e.printStackTrace();*/

        }
        return modbusMaster;
    }
    /**
     *
     * @param portName 串口名
     * @param baudRate 波特率
     * @param dataBits 数据位
     * @param stopBits 中止位
     * @param parity   校验位
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getSerialPortAsciiMaster(String portName, Integer baudRate, Integer dataBits,
                                                      Integer stopBits, Integer parity){
            // 设置串口参数，串口是COM1，波特率是9600
            // SerialPortWrapperImpl wrapper = new SerialPortWrapperImpl("COM2", 9600,SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, 0, 0);
            SerialPortWrapperImpl wrapper = new SerialPortWrapperImpl(portName, baudRate,
                    dataBits, stopBits, parity, 0, 0);
        ModbusMaster master = modbusFactory.createAsciiMaster(wrapper);
        try {
            //设置超时时间
            master.setTimeout(1000);
            //设置重连次数
            master.setRetries(3);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            log.error("串口连接异常~");
            e.printStackTrace();
        }
        return master;
    }

    /**
     * 读取[01 Coil Status 0x]类型 开关数据
     *
     * @param slaveId slaveId
     * @param offset  位置
     * @return 读取值
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    public static Boolean readCoilStatus(ModbusMaster master, int slaveId, int offset)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 01 Coil Status
        BaseLocator<Boolean> loc = BaseLocator.coilStatus(slaveId, offset);
        Boolean value = master.getValue(loc);
        return value;
    }
    /**
     * 读取[02 Input Status 0x]类型 开关数据
     *
     * @param slaveId slaveId
     * @param offset  位置
     * @return 读取值
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    public static Boolean readInputStatus(ModbusMaster master, int slaveId, int offset)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 02 inputStatus
        BaseLocator<Boolean> loc = BaseLocator.inputStatus(slaveId, offset);
        Boolean value = master.getValue(loc);
        return value;
    }

    /**
     * 读取[03 Holding Register类型 2x]模拟量数据
     *
     * @param slaveId  slave Id
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    public static Number readHoldingRegister(ModbusMaster master, int slaveId, int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 03 Holding Register类型数据读取
        BaseLocator<Number> loc = BaseLocator.holdingRegister(slaveId, offset, dataType);
        Number value = master.getValue(loc);
        return value;
    }

    /**
     * 读取[04 Input Registers 3x]类型 模拟量数据
     *
     * @param slaveId  slaveId
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return 返回结果
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    public static Number readInputRegisters(ModbusMaster master, int slaveId, int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 04 Input Registers类型数据读取
        BaseLocator<Number> loc = BaseLocator.inputRegister(slaveId, offset, dataType);
        Number value = master.getValue(loc);
        return value;
    }

    /**
     * @Title writeCoil
     * @Description: 写单个（线圈）开关量数据，相当于功能码：05H-写单个线圈
     * @params: [master,slaveId, writeOffset, writeValue]
     * @return: boolean
     * @throws: ModbusTransportException
     */
    public static boolean writeCoil(ModbusMaster master, int slaveId, int writeOffset, boolean writeValue) throws ModbusTransportException {
        WriteCoilRequest request = new WriteCoilRequest(slaveId, writeOffset, writeValue);
        WriteCoilResponse response = (WriteCoilResponse) master.send(request);
        return !response.isException();
    }

    /**
     * @Title writeCoils
     * @Description: 写多个开关量数据（线圈），相当于功能码：0FH-写多个线圈
     * @params: [master,slaveId, startOffset, data]
     * @return: boolean
     * @throws: ModbusTransportException
     */
    public static boolean writeCoils(ModbusMaster master, int slaveId, int startOffset, boolean[] data) throws ModbusTransportException {

        WriteCoilsRequest request = new WriteCoilsRequest(slaveId, startOffset, data);
        WriteCoilsResponse response = (WriteCoilsResponse) master.send(request);
        return !response.isException();

    }

    /**
     * @Title writeHoldingRegister
     * @Description: 写单个保持寄存器，相当于功能码：06H-写单个保持寄存器
     * @params: [master,slaveId, writeOffset, writeValue]
     * @return: boolean
     * @throws: [ModbusTransportException, ModbusInitException]
     */
    public static boolean writeHoldingRegister(ModbusMaster master, int slaveId, int writeOffset, short writeValue) throws ModbusTransportException, ModbusInitException {

        WriteRegisterRequest request = new WriteRegisterRequest(slaveId, writeOffset, writeValue);
        WriteRegisterResponse response = (WriteRegisterResponse) master.send(request);
        return !response.isException();

    }

    /**
     * @Title writeHoldingRegisters
     * @Description: 写多个保持寄存器，相当于功能码：10H-写多个保持寄存器
     * @params: [master, slaveId, startOffset, data]
     * @return: boolean
     * @throws: [ModbusTransportException, ModbusInitException]
     */
    public static boolean writeHoldingRegisters(ModbusMaster master, int slaveId, int startOffset, short[] data) throws ModbusTransportException, ModbusInitException {

        WriteRegistersRequest request = new WriteRegistersRequest(slaveId, startOffset, data);
        WriteRegistersResponse response = (WriteRegistersResponse) master.send(request);
        return !response.isException();
    }


}