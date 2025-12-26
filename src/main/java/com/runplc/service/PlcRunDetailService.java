package com.runplc.service;

import com.runplc.entity.PlcRunDetail;
import java.util.List;

public interface PlcRunDetailService {
    /**
     * 保存PLC运行详情
     * @param plcRunDetail PLC运行详情对象
     * @return 是否成功
     */
    boolean save(PlcRunDetail plcRunDetail);

    /**
     * 根据ID删除PLC运行详情
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    /**
     * 根据plc_run_id删除PLC运行详情
     * @param plcRunId plc_run表的ID
     * @return 是否成功
     */
    boolean deleteByPlcRunId(Integer plcRunId);

    /**
     * 根据plc_addr_id列表查询受影响的plc_run_id（去重）
     * @param plcAddrIds plc_addr表的ID列表
     * @return plc_run_id列表
     */
    List<Integer> getDistinctPlcRunIdsByPlcAddrIds(List<Integer> plcAddrIds);

    /**
     * 根据plc_addr_id列表批量删除plc_run_detail记录
     * @param plcAddrIds plc_addr表的ID列表
     * @return 是否成功
     */
    boolean deleteByPlcAddrIds(List<Integer> plcAddrIds);

    /**
     * 统计某个plc_run_id剩余的详情数量
     * @param plcRunId plc_run表的ID
     * @return 数量
     */
    int countByPlcRunId(Integer plcRunId);

    /**
     * 更新PLC运行详情
     * @param plcRunDetail PLC运行详情对象
     * @return 是否成功
     */
    boolean update(PlcRunDetail plcRunDetail);

    /**
     * 只更新sort_no字段
     * @param id 记录ID
     * @param sortNo 排序号
     * @return 是否成功
     */
    boolean updateSortNo(Integer id, Integer sortNo);

    /**
     * 根据ID查询PLC运行详情
     * @param id 记录ID
     * @return PLC运行详情对象
     */
    PlcRunDetail getById(Integer id);

    /**
     * 根据plc_run_id查询PLC运行详情
     * @param plcRunId plc_run表的ID
     * @return PLC运行详情列表
     */
    List<PlcRunDetail> getByPlcRunId(Integer plcRunId);

    /**
     * 查询所有PLC运行详情
     * @return PLC运行详情列表
     */
    List<PlcRunDetail> getAll();
}