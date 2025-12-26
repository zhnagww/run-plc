package com.runplc.service;

import com.runplc.entity.AppData;
import java.util.List;

public interface AppDataService {
    /**
     * 保存数据
     * @param appData 数据对象
     * @return 是否成功
     */
    boolean save(AppData appData);

    /**
     * 根据ID删除数据
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    /**
     * 更新数据
     * @param appData 数据对象
     * @return 是否成功
     */
    boolean update(AppData appData);

    /**
     * 根据ID查询数据
     * @param id 记录ID
     * @return 数据对象
     */
    AppData getById(Integer id);

    /**
     * 查询所有数据
     * @return 数据列表
     */
    List<AppData> getAll();

    /**
     * 根据dataKey查询数据
     * @param dataKey 键值
     * @return 数据对象
     */
    AppData getByDataKey(String dataKey);
}