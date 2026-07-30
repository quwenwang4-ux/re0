package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.entity.FishInfo;
import com.seafish.service.FishInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/fishes")
public class FishInfoController {

    private final FishInfoService fishInfoService;

    public FishInfoController(
            FishInfoService fishInfoService
    ) {
        this.fishInfoService = fishInfoService;
    }

    @GetMapping
    public ApiResponse<List<FishInfo>> listFishes() {
        List<FishInfo> fishes =
                fishInfoService.listFishes();

        return ApiResponse.success(fishes);
    }

    @GetMapping("/{id}")
    public ApiResponse<FishInfo> getFishById(
            @PathVariable Long id
    ) {
        FishInfo fishInfo =
                fishInfoService.getFishById(id);

        return ApiResponse.success(fishInfo);
    }
}