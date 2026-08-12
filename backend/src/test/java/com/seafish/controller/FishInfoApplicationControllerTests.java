package com.seafish.controller;

import com.seafish.controller.request.CreateFishInfoApplicationRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.request.ReviewFishInfoApplicationRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.entity.FishInfoApplication;
import com.seafish.mapper.FishInfoMapper;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.service.FishInfoApplicationService;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FishInfoApplicationControllerTests {

    @Autowired private FishInfoApplicationService service;
    @Autowired private UserService userService;
    @Autowired private SysUserRoleMapper roleMapper;
    @Autowired private FishInfoMapper fishInfoMapper;

    @Test
    @Transactional
    void approvesAddApplicationAndCreatesFishInfo() {
        UserResponse user = registerUser();
        UserResponse admin = registerAdmin();
        CreateFishInfoApplicationRequest create = validAddRequest();

        FishInfoApplication application = service.create(user.getId(), create);
        ReviewFishInfoApplicationRequest review = new ReviewFishInfoApplicationRequest();
        review.setDecision("APPROVED");
        review.setReviewComment("资料可靠");

        FishInfoApplication reviewed = service.review(application.getId(), admin.getId(), review);
        assertEquals("APPROVED", reviewed.getStatus());

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.seafish.entity.FishInfo> query =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("scientific_name", create.getScientificName());
        assertNotNull(fishInfoMapper.selectOne(query));
    }

    @Test
    @Transactional
    void correctionRequiresTargetFish() {
        UserResponse user = registerUser();
        CreateFishInfoApplicationRequest request = validAddRequest();
        request.setApplicationType("CORRECTION");

        com.seafish.exception.BusinessException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        com.seafish.exception.BusinessException.class,
                        () -> service.create(user.getId(), request)
                );
        assertEquals(40030, exception.getCode());
    }

    @Test
    @Transactional
    void preventsDuplicatePendingApplication() {
        UserResponse user = registerUser();
        CreateFishInfoApplicationRequest request = validAddRequest();
        service.create(user.getId(), request);

        com.seafish.exception.BusinessException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        com.seafish.exception.BusinessException.class,
                        () -> service.create(user.getId(), request)
                );
        assertEquals(40920, exception.getCode());
    }

    private CreateFishInfoApplicationRequest validAddRequest() {
        CreateFishInfoApplicationRequest request = new CreateFishInfoApplicationRequest();
        request.setApplicationType("ADD");
        request.setChineseName("申请测试鱼" + UUID.randomUUID());
        request.setScientificName("Testus." + UUID.randomUUID());
        request.setCategory("硬骨鱼纲");
        request.setSourceDescription("测试资料来源");
        request.setReason("系统尚未收录");
        return request;
    }

    private UserResponse registerAdmin() {
        UserResponse admin = registerUser();
        assertTrue(roleMapper.assignOrReactivateRole(admin.getId(), "ADMIN", null) >= 1);
        return admin;
    }

    private UserResponse registerUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("fish_apply_" + UUID.randomUUID().toString().replace("-", ""));
        request.setPassword("Ocean1234");
        return userService.register(request);
    }
}
