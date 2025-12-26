package com.runplc.modbus.serialPort;

import com.runplc.modbus.ModbusUtils;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import jssc.SerialPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialPortMasterTest {
    private static final int[] dataList = new int[]{2, 3, 22, 23, 4, 5, 6, 7, 24, 25, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 26, 29, 27, 30, 28, 31, 32, 33};
    public static void main(String[] args) throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        try {
            for (int i=0;i<dataList.length;i++){
                try {
                    System.out.println(dataList[i]);
                    ModbusMaster rtuMaster = ModbusUtils.getSerialPortRtuMaster("COM11", 9600, 8, 1, SerialPort.PARITY_NONE);
                    Number number = ModbusUtils.readHoldingRegister(rtuMaster, 1, 5, 2);
                    int i1 = number.intValue();
                    System.out.println(i1);

/*                    ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(1, 5, 8);
                    ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse)master.send(request);
                    if (response.isException()) {
                        System.out.println("Exception response: message=" + response.getExceptionMessage());
                    } else {
                        System.out.println(Arrays.toString(response.getShortData()));
*//*                        short[] list = response.getShortData();
                        for (int i = 0; i < list.length; i++) {
                            System.out.print(list[i] + " ");
                        }*//*
                    }*/


                }catch (Exception e){
                    System.out.println(e.getMessage());
                }






            }

        }catch (Exception e){
            log.error("error!!!!!");
            e.printStackTrace();

        }

    }





}