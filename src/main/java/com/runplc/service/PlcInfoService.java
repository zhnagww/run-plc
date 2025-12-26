package com.runplc.service;

import com.runplc.entity.PlcInfo;
import java.util.List;

public interface PlcInfoService {
    /**
     * 保存PLC信息
     * @param plcInfo PLC信息对象
     * @return 是否成功
     */
    boolean save(PlcInfo plcInfo);

    /**
     * 根据ID删除PLC信息
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    /**
     * 更新PLC信息
     * @param plcInfo PLC信息对象
     * @return 是否成功
     */
    boolean update(PlcInfo plcInfo);

    /**
     * 根据ID查询PLC信息
     * @param id 记录ID
     * @return PLC信息对象
     */
    PlcInfo getById(Integer id);

    /**
     * 查询所有PLC信息
     * @return PLC信息列表
     */
    List<PlcInfo> getAll();
    
    /**
     * 根据IP地址查询PLC信息
     * @param ipAddr IP地址
     * @return PLC信息对象
     */
    PlcInfo getByIpAddr(String ipAddr);

    PlcInfo getByIpAddrAndPortNo(String ipAddr, Integer portNo);
}