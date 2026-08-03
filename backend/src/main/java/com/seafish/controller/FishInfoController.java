package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.entity.FishInfo;
import com.seafish.service.FishInfoService;
import org.springframework.web.bind.annotation.*;
import com.seafish.controller.request.CreateFishRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.seafish.controller.request.UpdateFishRequest;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FishInfo> createFish(
            @Valid
            @RequestBody
            CreateFishRequest request
    ) {
        FishInfo fishInfo =
                fishInfoService.createFish(request);

        return ApiResponse.success(fishInfo);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFish(
            @PathVariable Long id
    ) {
        fishInfoService.deleteFish(id);

        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<FishInfo> updateFish(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateFishRequest request
    ) {
        FishInfo fishInfo =
                fishInfoService.updateFish(
                        id,
                        request
                );

        return ApiResponse.success(fishInfo);
    }
}