package com.seafish.service;

import com.seafish.entity.FishInfo;
import com.seafish.mapper.FishInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FishInfoService {

    private final FishInfoMapper fishInfoMapper;

    public FishInfoService(
            FishInfoMapper fishInfoMapper
    ) {
        this.fishInfoMapper = fishInfoMapper;
    }

    public List<FishInfo> listFishes() {
        return fishInfoMapper.selectList(null);
    }
}