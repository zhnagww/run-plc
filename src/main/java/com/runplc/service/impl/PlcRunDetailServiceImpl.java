package com.runplc.service.impl;

import com.runplc.entity.PlcRunDetail;
import com.runplc.mapper.PlcRunDetailMapper;
import com.runplc.service.PlcRunDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PlcRunDetailServiceImpl implements PlcRunDetailService {

    @Autowired
    private PlcRunDetailMapper plcRunDetailMapper;

    @Override
    public boolean save(PlcRunDetail plcRunDetail) {
        return plcRunDetailMapper.insert(plcRunDetail) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return plcRunDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByPlcRunId(Integer plcRunId) {
        return plcRunDetailMapper.deleteByPlcRunId(plcRunId) > 0;
    }

    @Override
    public List<Integer> getDistinctPlcRunIdsByPlcAddrIds(List<Integer> plcAddrIds) {
        if (plcAddrIds == null || plcAddrIds.isEmpty()) {
            return Collections.emptyList();
        }
        return plcRunDetailMapper.selectDistinctPlcRunIdsByPlcAddrIds(plcAddrIds);
    }

    @Override
    public boolean deleteByPlcAddrIds(List<Integer> plcAddrIds) {
        if (plcAddrIds == null || plcAddrIds.isEmpty()) {
            return true;
        }
        return plcRunDetailMapper.deleteByPlcAddrIds(plcAddrIds) >= 0;
    }

    @Override
    public int countByPlcRunId(Integer plcRunId) {
        return plcRunDetailMapper.countByPlcRunId(plcRunId);
    }

    @Override
    public boolean update(PlcRunDetail plcRunDetail) {
        return plcRunDetailMapper.update(plcRunDetail) > 0;
    }

    @Override
    public boolean updateSortNo(Integer id, Integer sortNo) {
        return plcRunDetailMapper.updateSortNo(id, sortNo) > 0;
    }

    @Override
    public PlcRunDetail getById(Integer id) {
        return plcRunDetailMapper.selectById(id);
    }

    @Override
    public List<PlcRunDetail> getByPlcRunId(Integer plcRunId) {
        return plcRunDetailMapper.selectByPlcRunId(plcRunId);
    }

    @Override
    public List<PlcRunDetail> getAll() {
        return plcRunDetailMapper.selectAll();
    }
}