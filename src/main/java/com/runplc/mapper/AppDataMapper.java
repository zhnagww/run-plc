package com.runplc.mapper;

import com.runplc.entity.AppData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppDataMapper {
    /**
     * 插入一条记录
     * @param appData 数据对象
     * @return 影响行数
     */
    int insert(AppData appData);

    /**
     * 根据ID删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新记录
     * @param appData 数据对象
     * @return 影响行数
     */
    int update(AppData appData);

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 数据对象
     */
    AppData selectById(@Param("id") Integer id);

    /**
     * 查询所有记录
     * @return 数据列表
     */
    List<AppData> selectAll();

    /**
     * 根据dataKey查询记录
     * @param dataKey 键值
     * @return 数据对象
     */
    AppData selectByDataKey(@Param("dataKey") String dataKey);
}