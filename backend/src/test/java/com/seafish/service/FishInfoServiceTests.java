package com.seafish.service;

import com.seafish.common.PageResponse;
import com.seafish.controller.request.CreateFishRequest;
import com.seafish.controller.request.UpdateFishRequest;
import com.seafish.entity.FishInfo;
import com.seafish.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SpringBootTest
class FishInfoServiceTests {

    @Autowired
    private FishInfoService fishInfoService;

    @Test
    @Transactional
    void searchesFishesByKeyword() {
        CreateFishRequest request = new CreateFishRequest();
        String uniqueName = "搜索测试鱼" + UUID.randomUUID();
        request.setChineseName(uniqueName);
        request.setScientificName("SearchTest-" + UUID.randomUUID());
        request.setSourceType("TEST");
        fishInfoService.createFish(request);

        PageResponse<FishInfo> result =
                fishInfoService.listFishes(
                        1,
                        10,
                        uniqueName,
                        null
                );

        assertEquals(1, result.getTotal());
        assertEquals(uniqueName, result.getRecords().get(0).getChineseName());
    }

    @Test
    void findsFishById() {
        PageResponse<FishInfo> fishPage =
                fishInfoService.listFishes(
                        1,
                        10,
                        null,
                        null
                );

        assertFalse(
                fishPage.getRecords().isEmpty()
        );

        Long existingId =
                fishPage
                        .getRecords()
                        .get(0)
                        .getId();

        FishInfo fishInfo =
                fishInfoService.getFishById(existingId);

        assertEquals(
                existingId,
                fishInfo.getId()
        );
    }

    @Test
    void rejectsMissingFish() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> fishInfoService.getFishById(
                                Long.MAX_VALUE
                        )
                );

        assertEquals(
                40401,
                exception.getCode()
        );

        assertEquals(
                "鱼类信息不存在",
                exception.getMessage()
        );
    }

    @Test
    @Transactional
    void logicallyDeletesFish() {
        CreateFishRequest request =
                new CreateFishRequest();

        request.setChineseName("逻辑删除测试鱼类");
        request.setScientificName(
                "DeleteTest-" + UUID.randomUUID()
        );
        request.setSourceType("TEST");

        FishInfo createdFish =
                fishInfoService.createFish(request);

        fishInfoService.deleteFish(
                createdFish.getId()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> fishInfoService.getFishById(
                                createdFish.getId()
                        )
                );

        assertEquals(
                40401,
                exception.getCode()
        );
    }

    @Test
    @Transactional
    void updatesFish() {
        CreateFishRequest createRequest =
                new CreateFishRequest();

        createRequest.setChineseName("修改前测试鱼类");
        createRequest.setScientificName(
                "UpdateTest-" + UUID.randomUUID()
        );
        createRequest.setSourceType("TEST");

        FishInfo createdFish =
                fishInfoService.createFish(
                        createRequest
                );

        UpdateFishRequest updateRequest =
                new UpdateFishRequest();

        updateRequest.setChineseName("修改后测试鱼类");
        updateRequest.setScientificName(
                createdFish.getScientificName()
        );
        updateRequest.setSourceType("TEST");
        updateRequest.setSourceDescription(
                "修改接口自动测试"
        );

        FishInfo updatedFish =
                fishInfoService.updateFish(
                        createdFish.getId(),
                        updateRequest
                );

        assertEquals(
                createdFish.getId(),
                updatedFish.getId()
        );

        assertEquals(
                "修改后测试鱼类",
                updatedFish.getChineseName()
        );

        assertEquals(
                "修改接口自动测试",
                updatedFish.getSourceDescription()
        );

        assertEquals(
                createdFish.getCreatedAt(),
                updatedFish.getCreatedAt()
        );

        assertFalse(
                updatedFish
                        .getUpdatedAt()
                        .isBefore(
                                createdFish.getUpdatedAt()
                        )
        );
    }
}
