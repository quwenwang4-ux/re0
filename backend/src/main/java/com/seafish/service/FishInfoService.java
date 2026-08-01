package com.seafish.service;

import com.seafish.entity.FishInfo;
import com.seafish.mapper.FishInfoMapper;
import org.springframework.stereotype.Service;
import com.seafish.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.CreateFishRequest;

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

    public FishInfo createFish(
            CreateFishRequest request
    ) {
        String scientificName =
                request.getScientificName();

        if (scientificName != null
                && !scientificName.isBlank()) {
            QueryWrapper<FishInfo> queryWrapper =
                    new QueryWrapper<>();

            queryWrapper.eq(
                    "scientific_name",
                    scientificName
            );

            Long existingCount =
                    fishInfoMapper.selectCount(queryWrapper);

            if (existingCount > 0) {
                throw new BusinessException(
                        40901,
                        "鱼类学名已存在"
                );
            }
        }

        FishInfo fishInfo = new FishInfo();//request转成entity

        fishInfo.setChineseName(
                request.getChineseName()
        );
        fishInfo.setScientificName(scientificName);
        fishInfo.setCategory(request.getCategory());
        fishInfo.setAppearance(request.getAppearance());
        fishInfo.setHabits(request.getHabits());
        fishInfo.setHabitat(request.getHabitat());
        fishInfo.setDistribution(
                request.getDistribution()
        );
        fishInfo.setProtectionLevel(
                request.getProtectionLevel()
        );
        fishInfo.setCoverImageUrl(
                request.getCoverImageUrl()
        );
        fishInfo.setSourceType(
                request.getSourceType()
        );
        fishInfo.setSourceDescription(
                request.getSourceDescription()
        );

        int insertedRows =
                fishInfoMapper.insert(fishInfo);

        if (insertedRows != 1) {
            throw new BusinessException(
                    50001,
                    "鱼类信息保存失败"
            );
        }

        return getFishById(fishInfo.getId());
    }

    public void deleteFish(Long id) {
        getFishById(id);

        int deletedRows =
                fishInfoMapper.deleteById(id);

        if (deletedRows != 1) {
            throw new BusinessException(
                    50002,
                    "鱼类信息删除失败"
            );
        }
    }
}