package com.runplc.mapper;

import com.runplc.entity.PlcRunDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlcRunDetailMapper {
    /**
     * 插入一条记录
     * @param plcRunDetail 数据对象
     * @return 影响行数
     */
    int insert(PlcRunDetail plcRunDetail);

    /**
     * 根据ID删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 根据plc_run_id删除记录
     * @param plcRunId plc_run表的ID
     * @return 影响行数
     */
    int deleteByPlcRunId(@Param("plcRunId") Integer plcRunId);

    /**
     * 更新记录
     * @param plcRunDetail 数据对象
     * @return 影响行数
     */
    int update(PlcRunDetail plcRunDetail);

    /**
     * 只更新sort_no字段
     * @param id 记录ID
     * @param sortNo 排序号
     * @return 影响行数
     */
    int updateSortNo(@Param("id") Integer id, @Param("sortNo") Integer sortNo);

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 数据对象
     */
    PlcRunDetail selectById(@Param("id") Integer id);

    /**
     * 根据plc_run_id查询记录
     * @param plcRunId plc_run表的ID
     * @return 数据列表
     */
    List<PlcRunDetail> selectByPlcRunId(@Param("plcRunId") Integer plcRunId);

    /**
     * 根据plc_addr_id列表查询受影响的plc_run_id（去重）
     * @param plcAddrIds plc_addr表的ID列表
     * @return plc_run_id列表
     */
    List<Integer> selectDistinctPlcRunIdsByPlcAddrIds(@Param("plcAddrIds") List<Integer> plcAddrIds);

    /**
     * 根据plc_addr_id列表批量删除plc_run_detail记录
     * @param plcAddrIds plc_addr表的ID列表
     * @return 影响行数
     */
    int deleteByPlcAddrIds(@Param("plcAddrIds") List<Integer> plcAddrIds);

    /**
     * 统计某个plc_run_id剩余的详情数量
     * @param plcRunId plc_run表的ID
     * @return 数量
     */
    int countByPlcRunId(@Param("plcRunId") Integer plcRunId);

    /**
     * 查询所有记录
     * @return 数据列表
     */
    List<PlcRunDetail> selectAll();
}