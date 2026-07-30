package com.seafish.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.entity.FishInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FishInfoMapperTests {

    @Autowired
    private FishInfoMapper fishInfoMapper;

    @Test
    void findsFishByScientificName() {
        QueryWrapper<FishInfo> queryWrapper =
                new QueryWrapper<>();

        queryWrapper.eq(
                "scientific_name",
                "Amphiprion ocellaris"
        );

        FishInfo fishInfo =
                fishInfoMapper.selectOne(queryWrapper);

        assertNotNull(fishInfo);

        assertEquals(
                "小丑鱼",
                fishInfo.getChineseName()
        );

        System.out.println(
                "查询到鱼类：" + fishInfo.getChineseName()
        );
    }
}