package com.seafish.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SysUserMapperTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void connectsToSysUserTable() {
        Long userCount =
                sysUserMapper.selectCount(null);

        //断言
        assertNotNull(userCount);

        System.out.println(
                "当前用户数量：" + userCount
        );
    }
}