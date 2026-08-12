package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.CreateVolunteerApplicationRequest;
import com.seafish.controller.response.VolunteerApplicationResponse;
import com.seafish.entity.SysUser;
import com.seafish.entity.VolunteerApplication;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.SysUserMapper;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.mapper.VolunteerApplicationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seafish.common.PageResponse;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.seafish.controller.request.ReviewVolunteerApplicationRequest;

import java.time.LocalDateTime;

import java.util.List;

@Service
public class VolunteerApplicationService {

    private final VolunteerApplicationMapper
            volunteerApplicationMapper;

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    public VolunteerApplicationService(
            VolunteerApplicationMapper
                    volunteerApplicationMapper,
            SysUserMapper sysUserMapper,
            SysUserRoleMapper sysUserRoleMapper
    ) {
        this.volunteerApplicationMapper =
                volunteerApplicationMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Transactional
    public VolunteerApplicationResponse createApplication(
            Long applicantId,
            CreateVolunteerApplicationRequest request
    ) {
        SysUser applicant =
                sysUserMapper.selectById(applicantId);

        if (applicant == null) {
            throw new BusinessException(
                    40402,
                    "用户不存在"
            );
        }

        if (!"ACTIVE".equals(applicant.getStatus())) {
            throw new BusinessException(
                    40301,
                    "账号已被停用"
            );
        }

        List<String> roles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                applicantId
                        );

        if (roles.contains("VOLUNTEER")) {
            throw new BusinessException(
                    40905,
                    "当前用户已经是志愿者"
            );
        }

        QueryWrapper<VolunteerApplication>
                pendingQuery =
                new QueryWrapper<>();

        pendingQuery
                .eq("applicant_id", applicantId)
                .eq("status", "PENDING");

        Long pendingCount =
                volunteerApplicationMapper
                        .selectCount(pendingQuery);

        if (pendingCount > 0) {
            throw new BusinessException(
                    40906,
                    "已有待审核的志愿者申请"
            );
        }

        VolunteerApplication application =
                new VolunteerApplication();

        application.setApplicantId(applicantId);
        application.setRealName(
                request.getRealName().trim()
        );
        application.setPhone(
                request.getPhone().trim()
        );
        application.setRegion(
                request.getRegion().trim()
        );
        application.setSkills(
                normalizeOptional(
                        request.getSkills()
                )
        );
        application.setReason(
                request.getReason().trim()
        );
        application.setStatus("PENDING");

        int insertedRows =
                volunteerApplicationMapper
                        .insert(application);

        if (insertedRows != 1) {
            throw new BusinessException(
                    50007,
                    "志愿者申请提交失败"
            );
        }

        VolunteerApplication savedApplication =
                volunteerApplicationMapper
                        .selectById(
                                application.getId()
                        );

        return toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<VolunteerApplicationResponse>
    getMyApplications(Long applicantId) {
        SysUser applicant =
                sysUserMapper.selectById(applicantId);

        if (applicant == null) {
            throw new BusinessException(
                    40402,
                    "用户不存在"
            );
        }

        if (!"ACTIVE".equals(applicant.getStatus())) {
            throw new BusinessException(
                    40301,
                    "账号已被停用"
            );
        }

        QueryWrapper<VolunteerApplication> query =
                new QueryWrapper<>();

        query
                .eq("applicant_id", applicantId)
                .orderByDesc("created_at");

        List<VolunteerApplication> applications =
                volunteerApplicationMapper
                        .selectList(query);

        return applications
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<VolunteerApplicationResponse>
    listApplications(
            String status,
            long page,
            long size
    ) {
        if (page < 1) {
            throw new BusinessException(
                    40005,
                    "页码不能小于1"
            );
        }

        if (size < 1 || size > 100) {
            throw new BusinessException(
                    40006,
                    "每页数量必须在1到100之间"
            );
        }

        String normalizedStatus =
                normalizeOptional(status);

        if (normalizedStatus != null) {
            normalizedStatus =
                    normalizedStatus.toUpperCase();

            boolean validStatus =
                    "PENDING".equals(normalizedStatus)
                            || "APPROVED".equals(
                            normalizedStatus
                    )
                            || "REJECTED".equals(
                            normalizedStatus
                    )
                            || "CANCELLED".equals(
                            normalizedStatus
                    );

            if (!validStatus) {
                throw new BusinessException(
                        40007,
                        "申请状态不合法"
                );
            }
        }

        QueryWrapper<VolunteerApplication> query =
                new QueryWrapper<>();

        if (normalizedStatus != null) {
            query.eq(
                    "status",
                    normalizedStatus
            );
        }

        query.orderByDesc("created_at");

        Page<VolunteerApplication> pageRequest =
                new Page<>(page, size);

        Page<VolunteerApplication> pageResult =
                volunteerApplicationMapper
                        .selectPage(
                                pageRequest,
                                query
                        );

        List<VolunteerApplicationResponse> records =
                pageResult
                        .getRecords()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new PageResponse<>(
                records,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getPages()
        );
    }

    @Transactional
    public VolunteerApplicationResponse reviewApplication(
            Long applicationId,
            Long reviewerId,
            ReviewVolunteerApplicationRequest request
    ) {
        SysUser reviewer =
                sysUserMapper.selectById(reviewerId);

        if (reviewer == null) {
            throw new BusinessException(
                    40402,
                    "审核用户不存在"
            );
        }

        if (!"ACTIVE".equals(reviewer.getStatus())) {
            throw new BusinessException(
                    40301,
                    "审核账号已被停用"
            );
        }

        List<String> reviewerRoles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                reviewerId
                        );

        if (!reviewerRoles.contains("ADMIN")) {
            throw new BusinessException(
                    40303,
                    "当前用户没有管理员权限"
            );
        }

        VolunteerApplication application =
                volunteerApplicationMapper
                        .selectById(applicationId);

        if (application == null) {
            throw new BusinessException(
                    40403,
                    "志愿者申请不存在"
            );
        }

        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException(
                    40907,
                    "申请已处理，请勿重复审批"
            );
        }

        String reviewStatus =
                request
                        .getStatus()
                        .trim()
                        .toUpperCase();

        String reviewComment =
                normalizeOptional(
                        request.getReviewComment()
                );

        if ("REJECTED".equals(reviewStatus)
                && reviewComment == null) {
            throw new BusinessException(
                    40008,
                    "拒绝申请时必须填写审批意见"
            );
        }

        LocalDateTime reviewedAt =
                LocalDateTime.now();

        UpdateWrapper<VolunteerApplication> update =
                new UpdateWrapper<>();

        update
                .eq("id", applicationId)
                .eq("status", "PENDING")
                .set("status", reviewStatus)
                .set("reviewer_id", reviewerId)
                .set("review_comment", reviewComment)
                .set("reviewed_at", reviewedAt);

        int updatedRows =
                volunteerApplicationMapper.update(
                        null,
                        update
                );

        if (updatedRows != 1) {
            throw new BusinessException(
                    40907,
                    "申请已处理，请勿重复审批"
            );
        }

        if ("APPROVED".equals(reviewStatus)) {
            int roleRows =
                    sysUserRoleMapper
                            .assignOrReactivateRole(
                                    application
                                            .getApplicantId(),
                                    "VOLUNTEER",
                                    reviewerId
                            );

            if (roleRows < 1) {
                throw new BusinessException(
                        50009,
                        "志愿者角色分配失败"
                );
            }
        }

        VolunteerApplication reviewedApplication =
                volunteerApplicationMapper
                        .selectById(applicationId);

        return toResponse(reviewedApplication);
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private VolunteerApplicationResponse toResponse(
            VolunteerApplication application
    ) {
        return new VolunteerApplicationResponse(
                application.getId(),
                application.getApplicantId(),
                application.getRealName(),
                application.getPhone(),
                application.getRegion(),
                application.getSkills(),
                application.getReason(),
                application.getStatus(),
                application.getReviewerId(),
                application.getReviewComment(),
                application.getReviewedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}