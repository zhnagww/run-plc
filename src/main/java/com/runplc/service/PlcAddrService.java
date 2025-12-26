package com.runplc.service;

import com.runplc.entity.PlcAddr;
import java.util.List;

public interface PlcAddrService {
    /**
     * 保存PLC地址信息
     * @param plcAddr PLC地址对象
     * @return 是否成功
     */
    boolean save(PlcAddr plcAddr);

    /**
     * 根据ID删除PLC地址信息
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    /**
     * 更新PLC地址信息
     * @param plcAddr PLC地址对象
     * @return 是否成功
     */
    boolean update(PlcAddr plcAddr);

    /**
     * 根据ID查询PLC地址信息
     * @param id 记录ID
     * @return PLC地址对象
     */
    PlcAddr getById(Integer id);

    /**
     * 查询所有PLC地址信息
     * @return PLC地址列表
     */
    List<PlcAddr> getAll();
    
    /**
     * 根据plc_info_id查询PLC地址信息
     * @param plcInfoId plc_info表的ID
     * @return PLC地址列表
     */
    List<PlcAddr> getByPlcInfoId(Integer plcInfoId);
    
    /**
     * 根据plc_info_id删除PLC地址信息
     * @param plcInfoId plc_info表的ID
     * @return 是否成功
     */
    boolean deleteByPlcInfoId(Integer plcInfoId);

    /**
     * 根据plc_info_id和plc_no查询PLC地址信息
     * @param plcInfoId plc_info表的ID
     * @param plcNo plc_no
     * @return PLC地址对象
     */
    PlcAddr getByPlcInfoIdAndPlcNo(Integer plcInfoId, Integer plcNo);
}