package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.response.FileUploadResponse;
import com.seafish.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files/rescue-images")
public class FileController {
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // 该接口保持登录保护，SecurityConfig 没有把 POST 加入公开规则。
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> upload(
            @RequestPart("image") MultipartFile image
    ) {
        String fileName = fileStorageService.savePublicRescueImage(image);
        return ApiResponse.success(new FileUploadResponse(
                fileName, "/api/files/rescue-images/" + fileName
        ));
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadPublicRescueImage(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        fileStorageService.detectContentType(
                                "public/rescue/" + fileName
                        )
                ))
                .body(resource);
    }
}
