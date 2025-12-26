package com.runplc.service;

import com.runplc.entity.PlcRun;
import java.util.List;

public interface PlcRunService {
    /**
     * 保存PLC运行信息
     * @param plcRun PLC运行对象
     * @return 是否成功
     */
    boolean save(PlcRun plcRun);

    /**
     * 根据ID删除PLC运行信息
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    /**
     * 更新PLC运行信息
     * @param plcRun PLC运行对象
     * @return 是否成功
     */
    boolean update(PlcRun plcRun);

    /**
     * 根据ID查询PLC运行信息
     * @param id 记录ID
     * @return PLC运行对象
     */
    PlcRun getById(Integer id);

    PlcRun getByName(String name);

    /**
     * 查询所有PLC运行信息
     * @return PLC运行列表
     */
    List<PlcRun> getAll();
}