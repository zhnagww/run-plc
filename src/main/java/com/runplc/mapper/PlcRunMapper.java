package com.runplc.mapper;

import com.runplc.entity.PlcRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlcRunMapper {
    /**
     * 插入一条记录
     * @param plcRun 数据对象
     * @return 影响行数
     */
    int insert(PlcRun plcRun);

    /**
     * 根据ID删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新记录
     * @param plcRun 数据对象
     * @return 影响行数
     */
    int update(PlcRun plcRun);

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 数据对象
     */
    PlcRun selectById(@Param("id") Integer id);

    /**
     * 查询所有记录
     * @return 数据列表
     */
    List<PlcRun> selectAll();

    /**
     * 按名称查询记录
     * @param name 名称
     * @return 数据对象
     */
    PlcRun selectByNameForUnique(@Param("name") String name);
}