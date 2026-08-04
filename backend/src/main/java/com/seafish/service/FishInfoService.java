package com.seafish.service;

import com.seafish.entity.FishInfo;
import com.seafish.mapper.FishInfoMapper;
import org.springframework.stereotype.Service;
import com.seafish.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.CreateFishRequest;
import com.seafish.controller.request.UpdateFishRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seafish.common.PageResponse;


@Service
public class FishInfoService {

    private final FishInfoMapper fishInfoMapper;

    public FishInfoService(
            FishInfoMapper fishInfoMapper
    ) {
        this.fishInfoMapper = fishInfoMapper;
    }

    public PageResponse<FishInfo> listFishes(
            long page,
            long size
    ) {
        if (page < 1) {
            throw new BusinessException(
                    40005,
                    "页码不能小于 1"
            );
        }

        if (size < 1 || size > 100) {
            throw new BusinessException(
                    40006,
                    "每页数量必须在 1 到 100 之间"
            );
        }

        Page<FishInfo> pageRequest =
                new Page<>(page, size);

        Page<FishInfo> pageResult =
                fishInfoMapper.selectPage(
                        pageRequest,
                        null
                );

        return new PageResponse<>(
                pageResult.getRecords(),
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getPages()
        );
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

    public FishInfo updateFish(
            Long id,
            UpdateFishRequest request
    ) {
        // [项目自定义方法] 查询原数据并检查是否存在
        FishInfo fishInfo =
                getFishById(id);

        String scientificName =
                request.getScientificName();

        // 学名不为空时，检查是否被其他鱼类使用
        if (scientificName != null
                && !scientificName.isBlank()) {
            // [MyBatis-Plus 提供的类]
            QueryWrapper<FishInfo> queryWrapper =
                    new QueryWrapper<>();

            queryWrapper
                    .eq(
                            "scientific_name",
                            scientificName
                    )
                    .ne(
                            "id",
                            id
                    );

            // [MyBatis-Plus BaseMapper 提供]
            Long existingCount =
                    fishInfoMapper
                            .selectCount(queryWrapper);

            if (existingCount > 0) {
                throw new BusinessException(
                        40901,
                        "鱼类学名已存在"
                );
            }
        }

        // 将请求中的新值设置到原来的实体对象
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

        // [MyBatis-Plus BaseMapper 提供]
        int updatedRows =
                fishInfoMapper.updateById(fishInfo);

        if (updatedRows != 1) {
            throw new BusinessException(
                    50003,
                    "鱼类信息修改失败"
            );
        }

        // [项目自定义方法] 返回数据库中的最新数据
        return getFishById(id);
    }
}