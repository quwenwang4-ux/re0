package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seafish.common.PageResponse;
import com.seafish.controller.request.CreateFishInfoApplicationRequest;
import com.seafish.controller.request.ReviewFishInfoApplicationRequest;
import com.seafish.entity.FishInfo;
import com.seafish.entity.FishInfoApplication;
import com.seafish.entity.SysUser;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.FishInfoApplicationMapper;
import com.seafish.mapper.FishInfoMapper;
import com.seafish.mapper.SysUserMapper;
import com.seafish.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FishInfoApplicationService {

    private final FishInfoApplicationMapper applicationMapper;
    private final FishInfoMapper fishInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper userRoleMapper;

    public FishInfoApplicationService(
            FishInfoApplicationMapper applicationMapper,
            FishInfoMapper fishInfoMapper,
            SysUserMapper sysUserMapper,
            SysUserRoleMapper userRoleMapper
    ) {
        this.applicationMapper = applicationMapper;
        this.fishInfoMapper = fishInfoMapper;
        this.sysUserMapper = sysUserMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Transactional
    public FishInfoApplication create(
            Long applicantId,
            CreateFishInfoApplicationRequest request
    ) {
        requireActiveUser(applicantId);
        String type = request.getApplicationType()
                .trim()
                .toUpperCase();

        if ("CORRECTION".equals(type)) {
            if (request.getTargetFishId() == null) {
                throw new BusinessException(
                        40030,
                        "纠错申请必须选择目标鱼类"
                );
            }
            if (fishInfoMapper.selectById(
                    request.getTargetFishId()
            ) == null) {
                throw new BusinessException(40401, "目标鱼类不存在");
            }
        } else if (request.getTargetFishId() != null) {
            throw new BusinessException(
                    40031,
                    "新增申请不能填写目标鱼类编号"
            );
        }

        QueryWrapper<FishInfoApplication> duplicate =
                new QueryWrapper<>();
        duplicate.eq("applicant_id", applicantId)
                .eq("application_type", type)
                .eq("status", "PENDING");
        if (request.getTargetFishId() == null) {
            duplicate.isNull("target_fish_id")
                    .eq("chinese_name", request.getChineseName().trim());
        } else {
            duplicate.eq("target_fish_id", request.getTargetFishId());
        }
        if (applicationMapper.selectCount(duplicate) > 0) {
            throw new BusinessException(
                    40920,
                    "已有相同的待审核申请"
            );
        }

        FishInfoApplication application = new FishInfoApplication();
        application.setApplicantId(applicantId);
        application.setApplicationType(type);
        application.setTargetFishId(request.getTargetFishId());
        copyRequest(request, application);
        application.setStatus("PENDING");

        if (applicationMapper.insert(application) != 1) {
            throw new BusinessException(50040, "鱼类资料申请保存失败");
        }
        return applicationMapper.selectById(application.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<FishInfoApplication> getMyApplications(
            Long applicantId,
            long page,
            long size
    ) {
        validatePage(page, size);
        requireActiveUser(applicantId);
        QueryWrapper<FishInfoApplication> query = new QueryWrapper<>();
        query.eq("applicant_id", applicantId)
                .orderByDesc("created_at")
                .orderByDesc("id");
        return selectPage(page, size, query);
    }

    @Transactional(readOnly = true)
    public PageResponse<FishInfoApplication> listApplications(
            String status,
            long page,
            long size
    ) {
        validatePage(page, size);
        QueryWrapper<FishInfoApplication> query = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            String normalized = status.trim().toUpperCase();
            if (!List.of("PENDING", "APPROVED", "REJECTED")
                    .contains(normalized)) {
                throw new BusinessException(40032, "申请状态不合法");
            }
            query.eq("status", normalized);
        }
        query.orderByDesc("created_at").orderByDesc("id");
        return selectPage(page, size, query);
    }

    @Transactional
    public FishInfoApplication review(
            Long applicationId,
            Long reviewerId,
            ReviewFishInfoApplicationRequest request
    ) {
        requireAdmin(reviewerId);
        FishInfoApplication application =
                applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(40440, "鱼类资料申请不存在");
        }
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException(40921, "申请已处理，请勿重复审核");
        }

        String decision = request.getDecision()
                .trim()
                .toUpperCase();
        String comment = normalize(request.getReviewComment());
        if ("REJECTED".equals(decision) && comment == null) {
            throw new BusinessException(
                    40033,
                    "拒绝申请时必须填写审核意见"
            );
        }

        if ("APPROVED".equals(decision)) {
            applyToFishInfo(application, reviewerId);
        }

        UpdateWrapper<FishInfoApplication> update =
                new UpdateWrapper<>();
        update.eq("id", applicationId)
                .eq("status", "PENDING")
                .set("status", decision)
                .set("reviewer_id", reviewerId)
                .set("review_comment", comment)
                .set("reviewed_at", LocalDateTime.now());

        if (applicationMapper.update(null, update) != 1) {
            throw new BusinessException(40921, "申请已处理，请勿重复审核");
        }
        return applicationMapper.selectById(applicationId);
    }

    private void applyToFishInfo(
            FishInfoApplication application,
            Long reviewerId
    ) {
        checkScientificName(
                application.getScientificName(),
                application.getTargetFishId()
        );

        FishInfo fish;
        if ("ADD".equals(application.getApplicationType())) {
            fish = new FishInfo();
        } else {
            fish = fishInfoMapper.selectById(
                    application.getTargetFishId()
            );
            if (fish == null) {
                throw new BusinessException(40401, "目标鱼类不存在");
            }
        }

        fish.setChineseName(application.getChineseName());
        fish.setScientificName(application.getScientificName());
        fish.setCategory(application.getCategory());
        fish.setAppearance(application.getAppearance());
        fish.setHabits(application.getHabits());
        fish.setHabitat(application.getHabitat());
        fish.setDistribution(application.getDistribution());
        fish.setProtectionLevel(application.getProtectionLevel());
        fish.setCoverImageUrl(application.getCoverImageUrl());
        fish.setSourceType("USER_APPLICATION");
        fish.setSourceDescription(application.getSourceDescription());

        int affected;
        if (fish.getId() == null) {
            fish.setCreatedBy(reviewerId);
            affected = fishInfoMapper.insert(fish);
        } else {
            affected = fishInfoMapper.updateById(fish);
        }
        if (affected != 1) {
            throw new BusinessException(50041, "正式鱼类资料更新失败");
        }
    }

    private void checkScientificName(
            String scientificName,
            Long excludedFishId
    ) {
        if (scientificName == null || scientificName.isBlank()) {
            return;
        }
        QueryWrapper<FishInfo> query = new QueryWrapper<>();
        query.eq("scientific_name", scientificName.trim());
        if (excludedFishId != null) {
            query.ne("id", excludedFishId);
        }
        if (fishInfoMapper.selectCount(query) > 0) {
            throw new BusinessException(40901, "鱼类学名已存在");
        }
    }

    private void copyRequest(
            CreateFishInfoApplicationRequest source,
            FishInfoApplication target
    ) {
        target.setChineseName(source.getChineseName().trim());
        target.setScientificName(normalize(source.getScientificName()));
        target.setCategory(normalize(source.getCategory()));
        target.setAppearance(normalize(source.getAppearance()));
        target.setHabits(normalize(source.getHabits()));
        target.setHabitat(normalize(source.getHabitat()));
        target.setDistribution(normalize(source.getDistribution()));
        target.setProtectionLevel(normalize(source.getProtectionLevel()));
        target.setCoverImageUrl(normalize(source.getCoverImageUrl()));
        target.setSourceDescription(source.getSourceDescription().trim());
        target.setReason(source.getReason().trim());
    }

    private PageResponse<FishInfoApplication> selectPage(
            long page,
            long size,
            QueryWrapper<FishInfoApplication> query
    ) {
        Page<FishInfoApplication> result = applicationMapper
                .selectPage(new Page<>(page, size), query);
        return new PageResponse<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages()
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

    private void requireAdmin(Long userId) {
        requireActiveUser(userId);
        if (!userRoleMapper.selectActiveRoleCodes(userId)
                .contains("ADMIN")) {
            throw new BusinessException(40303, "当前用户没有管理员权限");
        }
    }

    private void validatePage(long page, long size) {
        if (page < 1) {
            throw new BusinessException(40005, "页码不能小于1");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(40006, "每页数量必须在1到100之间");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
