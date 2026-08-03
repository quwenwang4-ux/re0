package com.seafish.service;

import com.seafish.entity.FishInfo;
import com.seafish.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.seafish.controller.request.CreateFishRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.seafish.controller.request.UpdateFishRequest;

@SpringBootTest
class FishInfoServiceTests {

    @Autowired
    private FishInfoService fishInfoService;

    @Test
    void findsFishById() {
        List<FishInfo> fishes =
                fishInfoService.listFishes();

        assertFalse(fishes.isEmpty());

        Long existingId =
                fishes.get(0).getId();

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