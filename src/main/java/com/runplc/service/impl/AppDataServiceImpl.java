package com.runplc.service.impl;

import com.runplc.entity.AppData;
import com.runplc.mapper.AppDataMapper;
import com.runplc.service.AppDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppDataServiceImpl implements AppDataService {

    @Autowired
    private AppDataMapper appDataMapper;

    @Override
    public boolean save(AppData appData) {
        return appDataMapper.insert(appData) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return appDataMapper.deleteById(id) > 0;
    }

    @Override
    public boolean update(AppData appData) {
        return appDataMapper.update(appData) > 0;
    }

    @Override
    public AppData getById(Integer id) {
        return appDataMapper.selectById(id);
    }

    @Override
    public List<AppData> getAll() {
        return appDataMapper.selectAll();
    }

    @Override
    public AppData getByDataKey(String dataKey) {
        return appDataMapper.selectByDataKey(dataKey);
    }
}