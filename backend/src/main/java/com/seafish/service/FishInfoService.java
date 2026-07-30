package com.seafish.service;

import com.seafish.entity.FishInfo;
import com.seafish.mapper.FishInfoMapper;
import org.springframework.stereotype.Service;
import com.seafish.exception.BusinessException;

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
    public FishInfo getFishById(Long id) {
        FishInfo fishInfo =
                fishInfoMapper.selectById(id);

        if (fishInfo == null) {
            throw new BusinessException(
                    40401,
                    "鱼类信息不存在"
            );
        }

        return fishInfo;
    }
}