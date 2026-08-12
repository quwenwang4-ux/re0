package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.common.PageResponse;
import com.seafish.controller.response.DetectionRecordResponse;
import com.seafish.service.DetectionService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(
            DetectionService detectionService
    ) {
        this.detectionService = detectionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DetectionRecordResponse> detect(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("image") MultipartFile image,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false)
            BigDecimal confidenceThreshold
    ) {
        return ApiResponse.success(
                detectionService.detect(
                        Long.valueOf(jwt.getSubject()),
                        image,
                        modelName,
                        confidenceThreshold
                )
        );
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<DetectionRecordResponse>>
    getMyRecords(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.success(
                detectionService.getMyRecords(
                        Long.valueOf(jwt.getSubject()),
                        page,
                        size
                )
        );
    }

    @GetMapping("/{recordId}")
    public ApiResponse<DetectionRecordResponse> getRecord(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recordId
    ) {
        return ApiResponse.success(
                detectionService.getOwnedRecord(
                        Long.valueOf(jwt.getSubject()),
                        recordId
                )
        );
    }

    @GetMapping("/{recordId}/image/{imageType}")
    public ResponseEntity<Resource> getImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recordId,
            @PathVariable String imageType
    ) {
        DetectionService.PrivateImage image =
                detectionService.loadOwnedImage(
                        Long.valueOf(jwt.getSubject()),
                        recordId,
                        imageType
                );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        image.contentType()
                ))
                .body(image.resource());
    }
}
