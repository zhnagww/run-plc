package com.runplc.service.impl;

import com.runplc.entity.PlcRun;
import com.runplc.mapper.PlcRunMapper;
import com.runplc.service.PlcRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlcRunServiceImpl implements PlcRunService {

    @Autowired
    private PlcRunMapper plcRunMapper;

    @Override
    public boolean save(PlcRun plcRun) {
        return plcRunMapper.insert(plcRun) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return plcRunMapper.deleteById(id) > 0;
    }

    @Override
    public boolean update(PlcRun plcRun) {
        return plcRunMapper.update(plcRun) > 0;
    }

    @Override
    public PlcRun getById(Integer id) {
        return plcRunMapper.selectById(id);
    }

    @Override
    public PlcRun getByName(String name) {
        return plcRunMapper.selectByNameForUnique(name);
    }

    @Override
    public List<PlcRun> getAll() {
        return plcRunMapper.selectAll();
    }
}