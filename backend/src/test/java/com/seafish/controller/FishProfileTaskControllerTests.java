package com.seafish.controller;

import com.seafish.controller.request.CreateFishRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.request.ReviewFishProfileTaskRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.entity.DetectionResult;
import com.seafish.entity.FishInfo;
import com.seafish.entity.FishProfileTask;
import com.seafish.mapper.DetectionResultMapper;
import com.seafish.mapper.DetectionRecordMapper;
import com.seafish.mapper.FishProfileTaskMapper;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.service.FishInfoService;
import com.seafish.service.FishProfileTaskService;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FishProfileTaskControllerTests {

    @Autowired private FishProfileTaskService service;
    @Autowired private FishProfileTaskMapper taskMapper;
    @Autowired private DetectionResultMapper resultMapper;
    @Autowired private DetectionRecordMapper recordMapper;
    @Autowired private FishInfoService fishInfoService;
    @Autowired private UserService userService;
    @Autowired private SysUserRoleMapper roleMapper;

    @Test
    @Transactional
    void resolvesTaskAndLinksDetectionResult() {
        UserResponse admin = registerAdmin();
        FishInfo fish = createFish();
        DetectionResult result = createDetectionResultWithoutFish();

        FishProfileTask task = new FishProfileTask();
        task.setDetectionResultId(result.getId());
        task.setClassName("待关联类别");
        task.setStatus("PENDING");
        assertEquals(1, taskMapper.insert(task));

        ReviewFishProfileTaskRequest request =
                new ReviewFishProfileTaskRequest();
        request.setDecision("RESOLVED");
        request.setFishId(fish.getId());
        request.setReviewComment("已确认对应资料");

        FishProfileTask reviewed = service.review(
                task.getId(), admin.getId(), request
        );

        assertEquals("RESOLVED", reviewed.getStatus());
        assertEquals(fish.getId(), resultMapper
                .selectById(result.getId()).getFishId());
    }

    private DetectionResult createDetectionResultWithoutFish() {
        com.seafish.entity.DetectionRecord record =
                new com.seafish.entity.DetectionRecord();
        UserResponse owner = registerUser();
        record.setUserId(owner.getId());
        record.setOriginalImageUrl("test/private.jpg");
        record.setModelName("test-model");
        record.setConfidenceThreshold(new BigDecimal("0.5000"));
        record.setStatus("SUCCESS");
        record.setStartedAt(java.time.LocalDateTime.now());
        record.setCompletedAt(java.time.LocalDateTime.now());
        assertEquals(1, recordMapper.insert(record));

        DetectionResult result = new DetectionResult();
        result.setRecordId(record.getId());
        result.setClassName("待关联类别");
        result.setConfidence(new BigDecimal("0.8800"));
        result.setBoxX1(BigDecimal.ZERO);
        result.setBoxY1(BigDecimal.ZERO);
        result.setBoxX2(BigDecimal.ONE);
        result.setBoxY2(BigDecimal.ONE);
        assertEquals(1, resultMapper.insert(result));
        return result;
    }

    private FishInfo createFish() {
        CreateFishRequest request = new CreateFishRequest();
        request.setChineseName("补全任务测试鱼" + UUID.randomUUID());
        request.setScientificName("ProfileTask-" + UUID.randomUUID());
        request.setSourceType("TEST");
        return fishInfoService.createFish(request);
    }

    private UserResponse registerAdmin() {
        UserResponse admin = registerUser();
        assertTrue(roleMapper.assignOrReactivateRole(
                admin.getId(), "ADMIN", null
        ) >= 1);
        return admin;
    }

    private UserResponse registerUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("profile_task_" + UUID.randomUUID()
                .toString().replace("-", ""));
        request.setPassword("Ocean1234");
        return userService.register(request);
    }
}
