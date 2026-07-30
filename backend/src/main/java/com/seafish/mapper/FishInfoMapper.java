package com.seafish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seafish.entity.FishInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FishInfoMapper
        extends BaseMapper<FishInfo> {
}