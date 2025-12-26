package com.runplc.service.impl;

import com.runplc.entity.PlcAddr;
import com.runplc.mapper.PlcAddrMapper;
import com.runplc.service.PlcAddrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlcAddrServiceImpl implements PlcAddrService {

    @Autowired
    private PlcAddrMapper plcAddrMapper;

    @Override
    public boolean save(PlcAddr plcAddr) {
        return plcAddrMapper.insert(plcAddr) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return plcAddrMapper.deleteById(id) > 0;
    }

    @Override
    public boolean update(PlcAddr plcAddr) {
        return plcAddrMapper.update(plcAddr) > 0;
    }

    @Override
    public PlcAddr getById(Integer id) {
        return plcAddrMapper.selectById(id);
    }

    @Override
    public List<PlcAddr> getAll() {
        return plcAddrMapper.selectAll();
    }
    
    @Override
    public List<PlcAddr> getByPlcInfoId(Integer plcInfoId) {
        return plcAddrMapper.selectByPlcInfoId(plcInfoId);
    }
    
    @Override
    public boolean deleteByPlcInfoId(Integer plcInfoId) {
        return plcAddrMapper.deleteByPlcInfoId(plcInfoId) >= 0;
    }

    @Override
    public PlcAddr getByPlcInfoIdAndPlcNo(Integer plcInfoId, Integer plcNo) {
        return plcAddrMapper.selectByPlcInfoIdAndPlcNo(plcInfoId, plcNo);
    }
}