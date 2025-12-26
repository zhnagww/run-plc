package com.runplc.mapper;

import com.runplc.entity.PlcInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlcInfoMapper {
    /**
     * 插入一条记录
     * @param plcInfo 数据对象
     * @return 影响行数
     */
    int insert(PlcInfo plcInfo);

    /**
     * 根据ID删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新记录
     * @param plcInfo 数据对象
     * @return 影响行数
     */
    int update(PlcInfo plcInfo);

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 数据对象
     */
    PlcInfo selectById(@Param("id") Integer id);

    /**
     * 查询所有记录
     * @return 数据列表
     */
    List<PlcInfo> selectAll();
    
    /**
     * 根据IP地址查询记录
     * @param ipAddr IP地址
     * @return 数据对象
     */
    PlcInfo selectByIpAddr(@Param("ipAddr") String ipAddr);

    PlcInfo selectByIpAddrAndPortNo(@Param("ipAddr") String ipAddr, @Param("portNo") Integer portNo);
}