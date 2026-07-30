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
}