package com.runplc.service.impl;

import com.runplc.entity.PlcInfo;
import com.runplc.mapper.PlcInfoMapper;
import com.runplc.service.PlcAddrService;
import com.runplc.service.PlcInfoService;
import com.runplc.service.PlcRunDetailService;
import com.runplc.service.PlcRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlcInfoServiceImpl implements PlcInfoService {

    @Autowired
    private PlcInfoMapper plcInfoMapper;
    
    @Autowired
    private PlcAddrService plcAddrService;

    @Autowired
    private PlcRunDetailService plcRunDetailService;

    @Autowired
    private PlcRunService plcRunService;

    @Override
    public boolean save(PlcInfo plcInfo) {
        return plcInfoMapper.insert(plcInfo) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Integer id) {
        List<Integer> plcAddrIds = new ArrayList<>();
        plcAddrService.getByPlcInfoId(id).forEach(addr -> {
            if (addr != null && addr.getId() != null) {
                plcAddrIds.add(addr.getId());
            }
        });

        List<Integer> affectedPlcRunIds = plcRunDetailService.getDistinctPlcRunIdsByPlcAddrIds(plcAddrIds);

        if (!plcAddrIds.isEmpty()) {
            plcRunDetailService.deleteByPlcAddrIds(plcAddrIds);
            for (Integer plcRunId : affectedPlcRunIds) {
                if (plcRunId == null) {
                    continue;
                }
                int remaining = plcRunDetailService.countByPlcRunId(plcRunId);
                if (remaining <= 0) {
                    plcRunService.deleteById(plcRunId);
                }
            }
        }

        // 删除关联的PLC地址信息
        plcAddrService.deleteByPlcInfoId(id);
        // 再删除PLC信息
        return plcInfoMapper.deleteById(id) > 0;
    }

    @Override
    public boolean update(PlcInfo plcInfo) {
        return plcInfoMapper.update(plcInfo) > 0;
    }

    @Override
    public PlcInfo getById(Integer id) {
        return plcInfoMapper.selectById(id);
    }

    @Override
    public List<PlcInfo> getAll() {
        return plcInfoMapper.selectAll();
    }
    
    @Override
    public PlcInfo getByIpAddr(String ipAddr) {
        return plcInfoMapper.selectByIpAddr(ipAddr);
    }

    @Override
    public PlcInfo getByIpAddrAndPortNo(String ipAddr, Integer portNo) {
        return plcInfoMapper.selectByIpAddrAndPortNo(ipAddr, portNo);
    }
}