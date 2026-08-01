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
}