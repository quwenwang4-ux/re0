package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seafish.common.PageResponse;
import com.seafish.controller.response.DetectionRecordResponse;
import com.seafish.controller.response.DetectionResultResponse;
import com.seafish.detection.AiDetectionOutput;
import com.seafish.detection.AiDetectionTarget;
import com.seafish.detection.DetectionClientException;
import com.seafish.detection.FishDetectionClient;
import com.seafish.entity.DetectionRecord;
import com.seafish.entity.DetectionResult;
import com.seafish.entity.FishInfo;
import com.seafish.entity.FishProfileTask;
import com.seafish.entity.SysUser;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.DetectionRecordMapper;
import com.seafish.mapper.DetectionResultMapper;
import com.seafish.mapper.FishInfoMapper;
import com.seafish.mapper.FishProfileTaskMapper;
import com.seafish.mapper.SysUserMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DetectionService {

    private final DetectionRecordMapper recordMapper;
    private final DetectionResultMapper resultMapper;
    private final FishInfoMapper fishInfoMapper;
    private final FishProfileTaskMapper fishProfileTaskMapper;
    private final SysUserMapper sysUserMapper;
    private final FileStorageService fileStorageService;
    private final FishDetectionClient detectionClient;
    private final String defaultModelName;

    public DetectionService(
            DetectionRecordMapper recordMapper,
            DetectionResultMapper resultMapper,
            FishInfoMapper fishInfoMapper,
            FishProfileTaskMapper fishProfileTaskMapper,
            SysUserMapper sysUserMapper,
            FileStorageService fileStorageService,
            FishDetectionClient detectionClient,
            @org.springframework.beans.factory.annotation.Value(
                    "${seafish.detection.default-model:demo-yolo-v1}"
            ) String defaultModelName
    ) {
        this.recordMapper = recordMapper;
        this.resultMapper = resultMapper;
        this.fishInfoMapper = fishInfoMapper;
        this.fishProfileTaskMapper = fishProfileTaskMapper;
        this.sysUserMapper = sysUserMapper;
        this.fileStorageService = fileStorageService;
        this.detectionClient = detectionClient;
        this.defaultModelName = defaultModelName;
    }

    @Transactional
    public DetectionRecordResponse detect(
            Long userId,
            MultipartFile image,
            String modelName,
            BigDecimal confidenceThreshold
    ) {
        requireActiveUser(userId);

        String normalizedModel = normalizeModel(modelName);
        BigDecimal normalizedThreshold =
                normalizeThreshold(confidenceThreshold);
        FileStorageService.StoredFile storedFile =
                fileStorageService.saveDetectionImage(image);

        LocalDateTime startedAt = LocalDateTime.now();
        DetectionRecord record = new DetectionRecord();
        record.setUserId(userId);
        record.setOriginalImageUrl(storedFile.storageKey());
        record.setOriginalFileName(storedFile.originalFileName());
        record.setModelName(normalizedModel);
        record.setConfidenceThreshold(normalizedThreshold);
        record.setStatus("PROCESSING");
        record.setStartedAt(startedAt);

        if (recordMapper.insert(record) != 1) {
            throw new BusinessException(50030, "识别记录创建失败");
        }

        AiDetectionOutput output = null;
        try {
            output = detectionClient.detect(
                    storedFile.path(),
                    storedFile.originalFileName(),
                    normalizedModel,
                    normalizedThreshold
            );
        } catch (DetectionClientException exception) {
            record.setStatus("FAILED");
            record.setErrorCode(exception.getErrorCode());
            record.setErrorMessage(exception.getMessage());
        } catch (RuntimeException exception) {
            record.setStatus("FAILED");
            record.setErrorCode("AI_UNKNOWN_ERROR");
            record.setErrorMessage("识别服务发生异常，请稍后重试");
        }

        if (output != null) {
            List<AiDetectionTarget> targets = output.targets()
                    .stream()
                    .filter(target -> target.confidence()
                            .compareTo(normalizedThreshold) >= 0)
                    .toList();

            for (AiDetectionTarget target : targets) {
                saveResult(record.getId(), target);
            }

            record.setResultImageUrl(
                    output.resultImageStorageKey()
            );
            if (targets.isEmpty()) {
                record.setStatus("NO_TARGET");
                record.setErrorCode("NO_TARGET");
                record.setErrorMessage(
                        "未识别到满足置信度要求的鱼类，请更换清晰图片或降低阈值"
                );
            } else {
                record.setStatus("SUCCESS");
            }
        }

        LocalDateTime completedAt = LocalDateTime.now();
        record.setCompletedAt(completedAt);
        record.setDurationMs(
                Duration.between(startedAt, completedAt).toMillis()
        );

        if (recordMapper.updateById(record) != 1) {
            throw new BusinessException(50031, "识别记录更新失败");
        }

        return getOwnedRecord(userId, record.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<DetectionRecordResponse> getMyRecords(
            Long userId,
            long page,
            long size
    ) {
        validatePage(page, size);
        requireActiveUser(userId);

        QueryWrapper<DetectionRecord> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .orderByDesc("created_at")
                .orderByDesc("id");

        Page<DetectionRecord> result = recordMapper.selectPage(
                new Page<>(page, size),
                query
        );
        List<DetectionRecordResponse> records = result.getRecords()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                records,
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages()
        );
    }

    @Transactional(readOnly = true)
    public DetectionRecordResponse getOwnedRecord(
            Long userId,
            Long recordId
    ) {
        DetectionRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(40430, "识别记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(40330, "无权查看该识别记录");
        }
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public PrivateImage loadOwnedImage(
            Long userId,
            Long recordId,
            String imageType
    ) {
        DetectionRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(40430, "识别记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(40330, "无权查看该识别图片");
        }

        String storageKey = "result".equalsIgnoreCase(imageType)
                ? record.getResultImageUrl()
                : record.getOriginalImageUrl();
        if (storageKey == null) {
            throw new BusinessException(40420, "识别图片不存在");
        }

        Resource resource = fileStorageService
                .loadPrivateFile(storageKey);
        return new PrivateImage(
                resource,
                fileStorageService.detectContentType(storageKey)
        );
    }

    private void saveResult(
            Long recordId,
            AiDetectionTarget target
    ) {
        FishInfo fish = findFish(target.className());
        DetectionResult result = new DetectionResult();
        result.setRecordId(recordId);
        result.setFishId(fish == null ? null : fish.getId());
        result.setClassName(target.className());
        result.setConfidence(target.confidence());
        result.setBoxX1(target.boxX1());
        result.setBoxY1(target.boxY1());
        result.setBoxX2(target.boxX2());
        result.setBoxY2(target.boxY2());

        if (resultMapper.insert(result) != 1) {
            throw new BusinessException(50032, "识别结果保存失败");
        }

        if (fish == null) {
            FishProfileTask task = new FishProfileTask();
            task.setDetectionResultId(result.getId());
            task.setClassName(target.className());
            task.setStatus("PENDING");
            if (fishProfileTaskMapper.insert(task) != 1) {
                throw new BusinessException(
                        50033,
                        "鱼类资料补全任务创建失败"
                );
            }
        }
    }

    private FishInfo findFish(String className) {
        QueryWrapper<FishInfo> query = new QueryWrapper<>();
        query.and(wrapper -> wrapper
                        .eq("chinese_name", className)
                        .or()
                        .eq("scientific_name", className))
                .last("LIMIT 1");
        return fishInfoMapper.selectOne(query);
    }

    private DetectionRecordResponse toResponse(
            DetectionRecord record
    ) {
        QueryWrapper<DetectionResult> query = new QueryWrapper<>();
        query.eq("record_id", record.getId())
                .orderByDesc("confidence")
                .orderByAsc("id");

        List<DetectionResultResponse> results = resultMapper
                .selectList(query)
                .stream()
                .map(this::toResultResponse)
                .toList();

        return new DetectionRecordResponse(
                record.getId(),
                record.getUserId(),
                record.getOriginalFileName(),
                "/api/detections/" + record.getId() + "/image/original",
                record.getResultImageUrl() == null
                        ? null
                        : "/api/detections/" + record.getId() + "/image/result",
                record.getModelName(),
                record.getConfidenceThreshold(),
                record.getStatus(),
                record.getErrorCode(),
                record.getErrorMessage(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getDurationMs(),
                record.getCreatedAt(),
                results
        );
    }

    private DetectionResultResponse toResultResponse(
            DetectionResult result
    ) {
        FishInfo fish = result.getFishId() == null
                ? null
                : fishInfoMapper.selectById(result.getFishId());

        return new DetectionResultResponse(
                result.getId(),
                result.getFishId(),
                result.getClassName(),
                result.getConfidence(),
                result.getBoxX1(),
                result.getBoxY1(),
                result.getBoxX2(),
                result.getBoxY2(),
                fish == null ? null : fish.getChineseName(),
                fish == null ? null : fish.getScientificName(),
                fish == null ? null : fish.getCategory(),
                fish == null ? null : fish.getHabits(),
                fish == null ? null : fish.getHabitat(),
                fish == null ? null : fish.getDistribution(),
                fish == null ? null : fish.getProtectionLevel(),
                fish == null
        );
    }

    private void requireActiveUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40402, "用户不存在");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(40301, "用户账号已被停用");
        }
    }

    private String normalizeModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return defaultModelName;
        }
        String normalized = modelName.trim();
        if (normalized.length() > 100) {
            throw new BusinessException(40023, "模型名称不能超过100个字符");
        }
        return normalized;
    }

    private BigDecimal normalizeThreshold(BigDecimal threshold) {
        BigDecimal normalized = threshold == null
                ? new BigDecimal("0.5000")
                : threshold;
        if (normalized.compareTo(new BigDecimal("0.1000")) < 0
                || normalized.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(
                    40024,
                    "置信度阈值必须在0.1到1之间"
            );
        }
        return normalized;
    }

    private void validatePage(long page, long size) {
        if (page < 1) {
            throw new BusinessException(40005, "页码不能小于1");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(
                    40006,
                    "每页数量必须在1到100之间"
            );
        }
    }

    public record PrivateImage(
            Resource resource,
            String contentType
    ) {
    }
}
