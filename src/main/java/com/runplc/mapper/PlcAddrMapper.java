package com.runplc.mapper;

import com.runplc.entity.PlcAddr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlcAddrMapper {
    /**
     * 插入一条记录
     * @param plcAddr 数据对象
     * @return 影响行数
     */
    int insert(PlcAddr plcAddr);

    /**
     * 根据ID删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新记录
     * @param plcAddr 数据对象
     * @return 影响行数
     */
    int update(PlcAddr plcAddr);

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 数据对象
     */
    PlcAddr selectById(@Param("id") Integer id);

    /**
     * 查询所有记录
     * @return 数据列表
     */
    List<PlcAddr> selectAll();
    
    /**
     * 根据plc_info_id查询记录
     * @param plcInfoId plc_info表的ID
     * @return 数据列表
     */
    List<PlcAddr> selectByPlcInfoId(@Param("plcInfoId") Integer plcInfoId);
    
    /**
     * 根据plc_info_id删除记录
     * @param plcInfoId plc_info表的ID
     * @return 影响行数
     */
    int deleteByPlcInfoId(@Param("plcInfoId") Integer plcInfoId);

    /**
     * 根据plc_info_id和plc_no查询记录
     * @param plcInfoId plc_info表的ID
     * @param plcNo 点位号
     * @return 数据对象
     */
    PlcAddr selectByPlcInfoIdAndPlcNo(@Param("plcInfoId") Integer plcInfoId, @Param("plcNo") Integer plcNo);
}