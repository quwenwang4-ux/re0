package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.CreateRescueOrderRequest;
import com.seafish.controller.response.RescueOrderImageResponse;
import com.seafish.controller.response.RescueOrderResponse;
import com.seafish.entity.RescueOrder;
import com.seafish.entity.RescueOrderImage;
import com.seafish.entity.SysUser;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.RescueOrderImageMapper;
import com.seafish.mapper.RescueOrderMapper;
import com.seafish.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seafish.common.PageResponse;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.seafish.controller.request.ReviewRescueOrderRequest;
import com.seafish.mapper.SysUserRoleMapper;
import java.time.LocalDateTime;
import com.seafish.controller.response.PublicRescueOrderResponse;
import com.seafish.controller.request.CompleteRescueOrderRequest;
import com.seafish.controller.request.ConfirmRescueCompletionRequest;


@Service
public class RescueOrderService {

    private final RescueOrderMapper rescueOrderMapper;

    private final RescueOrderImageMapper
            rescueOrderImageMapper;

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper
            sysUserRoleMapper;



    public RescueOrderService(
            RescueOrderMapper rescueOrderMapper,
            RescueOrderImageMapper
                    rescueOrderImageMapper,
            SysUserMapper sysUserMapper,
            SysUserRoleMapper sysUserRoleMapper
    ) {
        this.rescueOrderMapper = rescueOrderMapper;
        this.rescueOrderImageMapper =
                rescueOrderImageMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper =
                sysUserRoleMapper;
    }
    @Transactional
    public RescueOrderResponse createOrder(
            Long reporterId,
            CreateRescueOrderRequest request
    ) {
        SysUser reporter =
                sysUserMapper.selectById(reporterId);

        if (reporter == null) {
            throw new BusinessException(
                    40402,
                    "上报用户不存在"
            );
        }

        if (!"ACTIVE".equals(reporter.getStatus())) {
            throw new BusinessException(
                    40301,
                    "上报账号已被停用"
            );
        }

        String locationText =
                normalizeOptional(
                        request.getLocationText()
                );

        BigDecimal latitude =
                request.getLatitude();

        BigDecimal longitude =
                request.getLongitude();

        boolean hasLatitude = latitude != null;
        boolean hasLongitude = longitude != null;

        if (hasLatitude != hasLongitude) {
            throw new BusinessException(
                    40010,
                    "经度和纬度必须同时提供"
            );
        }

        boolean hasCoordinates =
                hasLatitude && hasLongitude;

        if (locationText == null
                && !hasCoordinates) {
            throw new BusinessException(
                    40009,
                    "位置描述和经纬度至少提供一种"
            );
        }

        RescueOrder order = new RescueOrder();

        order.setReporterId(reporterId);
        order.setIssueType(
                request.getIssueType().trim()
        );
        order.setTitle(
                request.getTitle().trim()
        );
        order.setDescription(
                request.getDescription().trim()
        );
        order.setLocationText(locationText);
        order.setLatitude(latitude);
        order.setLongitude(longitude);
        order.setStatus("PENDING_REVIEW");

        int insertedOrderRows =
                rescueOrderMapper.insert(order);

        if (insertedOrderRows != 1) {
            throw new BusinessException(
                    50010,
                    "救助工单创建失败"
            );
        }

        List<String> imageUrls =
                request.getImageUrls();

        if (imageUrls != null) {
            for (int index = 0;
                 index < imageUrls.size();
                 index++) {
                RescueOrderImage image =
                        new RescueOrderImage();

                image.setRescueOrderId(
                        order.getId()
                );
                image.setImageType("REPORT");
                image.setImageUrl(
                        imageUrls
                                .get(index)
                                .trim()
                );
                image.setUploadedBy(reporterId);
                image.setSortOrder(index);

                int insertedImageRows =
                        rescueOrderImageMapper
                                .insert(image);

                if (insertedImageRows != 1) {
                    throw new BusinessException(
                            50011,
                            "工单图片保存失败"
                    );
                }
            }
        }

        RescueOrder savedOrder =
                rescueOrderMapper.selectById(
                        order.getId()
                );

        List<RescueOrderImageResponse> images =
                loadImageResponses(order.getId());

        return toResponse(savedOrder, images);
    }

    @Transactional(readOnly = true)
    public PageResponse<RescueOrderResponse>
    getMyOrders(
            Long reporterId,
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

        SysUser reporter =
                sysUserMapper.selectById(reporterId);

        if (reporter == null) {
            throw new BusinessException(
                    40402,
                    "上报用户不存在"
            );
        }

        if (!"ACTIVE".equals(reporter.getStatus())) {
            throw new BusinessException(
                    40301,
                    "上报账号已被停用"
            );
        }

        QueryWrapper<RescueOrder> query =
                new QueryWrapper<>();

        query
                .eq("reporter_id", reporterId)
                .orderByDesc("created_at");

        Page<RescueOrder> pageRequest =
                new Page<>(page, size);

        Page<RescueOrder> pageResult =
                rescueOrderMapper.selectPage(
                        pageRequest,
                        query
                );

        List<RescueOrderResponse> records =
                pageResult
                        .getRecords()
                        .stream()
                        .map(order ->
                                toResponse(
                                        order,
                                        loadImageResponses(
                                                order.getId()
                                        )
                                )
                        )
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
    public RescueOrderResponse acceptOrder(
            Long orderId,
            Long volunteerId
    ) {
        SysUser volunteer =
                sysUserMapper.selectById(volunteerId);

        if (volunteer == null) {
            throw new BusinessException(
                    40402,
                    "志愿者用户不存在"
            );
        }

        if (!"ACTIVE".equals(volunteer.getStatus())) {
            throw new BusinessException(
                    40301,
                    "志愿者账号已被停用"
            );
        }

        List<String> volunteerRoles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                volunteerId
                        );

        if (!volunteerRoles.contains("VOLUNTEER")) {
            throw new BusinessException(
                    40304,
                    "当前用户不是志愿者"
            );
        }

        RescueOrder order =
                rescueOrderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(
                    40404,
                    "救助工单不存在"
            );
        }

        if (!"OPEN".equals(order.getStatus())) {
            throw new BusinessException(
                    40909,
                    "工单已被接取或当前不可接取"
            );
        }

        LocalDateTime acceptedAt =
                LocalDateTime.now();

        UpdateWrapper<RescueOrder> update =
                new UpdateWrapper<>();

        update
                .eq("id", orderId)
                .eq("status", "OPEN")
                .set("status", "ACCEPTED")
                .set("volunteer_id", volunteerId)
                .set("accepted_at", acceptedAt);

        int updatedRows =
                rescueOrderMapper.update(
                        null,
                        update
                );

        if (updatedRows != 1) {
            throw new BusinessException(
                    40909,
                    "工单已被其他志愿者接取"
            );
        }

        RescueOrder acceptedOrder =
                rescueOrderMapper.selectById(orderId);

        List<RescueOrderImageResponse> images =
                loadImageResponses(orderId);

        return toResponse(
                acceptedOrder,
                images
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<RescueOrderResponse>
    getVolunteerOrders(
            Long volunteerId,
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

        SysUser volunteer =
                sysUserMapper.selectById(volunteerId);

        if (volunteer == null) {
            throw new BusinessException(
                    40402,
                    "志愿者用户不存在"
            );
        }

        if (!"ACTIVE".equals(volunteer.getStatus())) {
            throw new BusinessException(
                    40301,
                    "志愿者账号已被停用"
            );
        }

        List<String> volunteerRoles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                volunteerId
                        );

        if (!volunteerRoles.contains("VOLUNTEER")) {
            throw new BusinessException(
                    40304,
                    "当前用户不是志愿者"
            );
        }

        QueryWrapper<RescueOrder> query =
                new QueryWrapper<>();

        query
                .eq("volunteer_id", volunteerId)
                .orderByDesc("accepted_at")
                .orderByDesc("id");

        Page<RescueOrder> pageRequest =
                new Page<>(page, size);

        Page<RescueOrder> pageResult =
                rescueOrderMapper.selectPage(
                        pageRequest,
                        query
                );

        List<RescueOrderResponse> records =
                pageResult
                        .getRecords()
                        .stream()
                        .map(order ->
                                toResponse(
                                        order,
                                        loadImageResponses(
                                                order.getId()
                                        )
                                )
                        )
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
    public RescueOrderResponse submitCompletion(
            Long orderId,
            Long volunteerId,
            CompleteRescueOrderRequest request
    ) {
        SysUser volunteer =
                sysUserMapper.selectById(volunteerId);

        if (volunteer == null) {
            throw new BusinessException(
                    40402,
                    "志愿者用户不存在"
            );
        }

        if (!"ACTIVE".equals(volunteer.getStatus())) {
            throw new BusinessException(
                    40301,
                    "志愿者账号已被停用"
            );
        }

        List<String> volunteerRoles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                volunteerId
                        );

        if (!volunteerRoles.contains("VOLUNTEER")) {
            throw new BusinessException(
                    40304,
                    "当前用户不是志愿者"
            );
        }

        RescueOrder order =
                rescueOrderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(
                    40404,
                    "救助工单不存在"
            );
        }

        if (!volunteerId.equals(
                order.getVolunteerId()
        )) {
            throw new BusinessException(
                    40305,
                    "只能提交自己接取工单的完成反馈"
            );
        }

        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new BusinessException(
                    40910,
                    "工单当前不能提交完成反馈"
            );
        }

        String completionDescription =
                request
                        .getCompletionDescription()
                        .trim();

        LocalDateTime submittedAt =
                LocalDateTime.now();

        UpdateWrapper<RescueOrder> update =
                new UpdateWrapper<>();

        update
                .eq("id", orderId)
                .eq("status", "ACCEPTED")
                .eq("volunteer_id", volunteerId)
                .set(
                        "status",
                        "COMPLETION_PENDING"
                )
                .set(
                        "completion_description",
                        completionDescription
                )
                .set(
                        "completion_submitted_at",
                        submittedAt
                )
                .set("confirmer_id", null)
                .set("confirm_comment", null)
                .set("confirmed_at", null);

        int updatedRows =
                rescueOrderMapper.update(
                        null,
                        update
                );

        if (updatedRows != 1) {
            throw new BusinessException(
                    40910,
                    "工单状态已经改变，请勿重复提交"
            );
        }

        List<String> imageUrls =
                request.getImageUrls();

        if (imageUrls != null) {
            for (
                    int index = 0;
                    index < imageUrls.size();
                    index++
            ) {
                RescueOrderImage image =
                        new RescueOrderImage();

                image.setRescueOrderId(orderId);
                image.setImageType("COMPLETION");
                image.setImageUrl(
                        imageUrls
                                .get(index)
                                .trim()
                );
                image.setUploadedBy(volunteerId);
                image.setSortOrder(index);

                int insertedRows =
                        rescueOrderImageMapper
                                .insert(image);

                if (insertedRows != 1) {
                    throw new BusinessException(
                            50012,
                            "完成反馈图片保存失败"
                    );
                }
            }
        }

        RescueOrder completedOrder =
                rescueOrderMapper.selectById(orderId);

        List<RescueOrderImageResponse> images =
                loadImageResponses(orderId);

        return toResponse(
                completedOrder,
                images
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<RescueOrderResponse>
    listOrders(
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
                    "PENDING_REVIEW".equals(
                            normalizedStatus
                    )
                            || "REJECTED".equals(
                            normalizedStatus
                    )
                            || "OPEN".equals(
                            normalizedStatus
                    )
                            || "ACCEPTED".equals(
                            normalizedStatus
                    )
                            || "COMPLETION_PENDING".equals(
                            normalizedStatus
                    )
                            || "COMPLETED".equals(
                            normalizedStatus
                    )
                            || "CANCELLED".equals(
                            normalizedStatus
                    );

            if (!validStatus) {
                throw new BusinessException(
                        40012,
                        "工单状态不合法"
                );
            }
        }

        QueryWrapper<RescueOrder> query =
                new QueryWrapper<>();

        if (normalizedStatus != null) {
            query.eq(
                    "status",
                    normalizedStatus
            );
        }

        query.orderByDesc("created_at");

        Page<RescueOrder> pageRequest =
                new Page<>(page, size);

        Page<RescueOrder> pageResult =
                rescueOrderMapper.selectPage(
                        pageRequest,
                        query
                );

        List<RescueOrderResponse> records =
                pageResult
                        .getRecords()
                        .stream()
                        .map(order ->
                                toResponse(
                                        order,
                                        loadImageResponses(
                                                order.getId()
                                        )
                                )
                        )
                        .toList();

        return new PageResponse<>(
                records,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getPages()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicRescueOrderResponse>
    getPublicOrders(
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

        QueryWrapper<RescueOrder> query =
                new QueryWrapper<>();

        query.in(
                "status",
                List.of(
                        "OPEN",
                        "ACCEPTED",
                        "COMPLETION_PENDING",
                        "COMPLETED"
                )
        );
        query.orderByDesc("published_at");

        Page<RescueOrder> pageRequest =
                new Page<>(page, size);

        Page<RescueOrder> pageResult =
                rescueOrderMapper.selectPage(
                        pageRequest,
                        query
                );

        List<PublicRescueOrderResponse> records =
                pageResult
                        .getRecords()
                        .stream()
                        .map(order ->
                                toPublicResponse(order)
                        )
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
    public RescueOrderResponse reviewOrder(
            Long orderId,
            Long reviewerId,
            ReviewRescueOrderRequest request
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

        RescueOrder order =
                rescueOrderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(
                    40404,
                    "救助工单不存在"
            );
        }

        if (!"PENDING_REVIEW".equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    40908,
                    "工单已审核，请勿重复处理"
            );
        }

        String decision =
                request
                        .getDecision()
                        .trim()
                        .toUpperCase();

        String reviewComment =
                normalizeOptional(
                        request.getReviewComment()
                );

        if ("REJECTED".equals(decision)
                && reviewComment == null) {
            throw new BusinessException(
                    40011,
                    "拒绝工单时必须填写审核意见"
            );
        }

        String nextStatus =
                "APPROVED".equals(decision)
                        ? "OPEN"
                        : "REJECTED";

        LocalDateTime reviewedAt =
                LocalDateTime.now();

        UpdateWrapper<RescueOrder> update =
                new UpdateWrapper<>();

        update
                .eq("id", orderId)
                .eq("status", "PENDING_REVIEW")
                .set("status", nextStatus)
                .set("reviewer_id", reviewerId)
                .set("review_comment", reviewComment)
                .set("reviewed_at", reviewedAt);

        if ("APPROVED".equals(decision)) {
            update.set(
                    "published_at",
                    reviewedAt
            );
        }

        int updatedRows =
                rescueOrderMapper.update(
                        null,
                        update
                );

        if (updatedRows != 1) {
            throw new BusinessException(
                    40908,
                    "工单已审核，请勿重复处理"
            );
        }

        RescueOrder reviewedOrder =
                rescueOrderMapper.selectById(orderId);

        List<RescueOrderImageResponse> images =
                loadImageResponses(orderId);

        return toResponse(
                reviewedOrder,
                images
        );
    }

    @Transactional
    public RescueOrderResponse confirmCompletion(
            Long orderId,
            Long confirmerId,
            ConfirmRescueCompletionRequest request
    ) {
        SysUser confirmer =
                sysUserMapper.selectById(confirmerId);

        if (confirmer == null) {
            throw new BusinessException(
                    40402,
                    "确认用户不存在"
            );
        }

        if (!"ACTIVE".equals(confirmer.getStatus())) {
            throw new BusinessException(
                    40301,
                    "确认账号已被停用"
            );
        }

        List<String> confirmerRoles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                confirmerId
                        );

        if (!confirmerRoles.contains("ADMIN")) {
            throw new BusinessException(
                    40303,
                    "当前用户没有管理员权限"
            );
        }

        RescueOrder order =
                rescueOrderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(
                    40404,
                    "救助工单不存在"
            );
        }

        if (!"COMPLETION_PENDING".equals(
                order.getStatus()
        )) {
            throw new BusinessException(
                    40911,
                    "工单当前不处于待确认状态"
            );
        }

        String decision =
                request
                        .getDecision()
                        .trim()
                        .toUpperCase();

        String confirmComment =
                normalizeOptional(
                        request.getConfirmComment()
                );

        if ("REJECTED".equals(decision)
                && confirmComment == null) {
            throw new BusinessException(
                    40013,
                    "驳回完成反馈时必须填写确认意见"
            );
        }

        String nextStatus =
                "CONFIRMED".equals(decision)
                        ? "COMPLETED"
                        : "ACCEPTED";

        LocalDateTime confirmedAt =
                LocalDateTime.now();

        UpdateWrapper<RescueOrder> update =
                new UpdateWrapper<>();

        update
                .eq("id", orderId)
                .eq("status", "COMPLETION_PENDING")
                .set("status", nextStatus)
                .set("confirmer_id", confirmerId)
                .set("confirm_comment", confirmComment)
                .set("confirmed_at", confirmedAt);

        if ("REJECTED".equals(decision)) {
            update
                    .set("completion_description", null)
                    .set("completion_submitted_at", null);
        }

        int updatedRows =
                rescueOrderMapper.update(
                        null,
                        update
                );

        if (updatedRows != 1) {
            throw new BusinessException(
                    40911,
                    "工单状态已经改变，请勿重复确认"
            );
        }

        if ("REJECTED".equals(decision)) {
            QueryWrapper<RescueOrderImage> imageQuery =
                    new QueryWrapper<>();

            imageQuery
                    .eq("rescue_order_id", orderId)
                    .eq("image_type", "COMPLETION");

            rescueOrderImageMapper.delete(imageQuery);
        }

        RescueOrder confirmedOrder =
                rescueOrderMapper.selectById(orderId);

        List<RescueOrderImageResponse> images =
                loadImageResponses(orderId);

        return toResponse(
                confirmedOrder,
                images
        );
    }

    private List<RescueOrderImageResponse>
    loadImageResponses(Long orderId) {
        QueryWrapper<RescueOrderImage> query =
                new QueryWrapper<>();

        query
                .eq("rescue_order_id", orderId)
                .orderByAsc("sort_order")
                .orderByAsc("id");

        List<RescueOrderImage> images =
                rescueOrderImageMapper
                        .selectList(query);

        return images
                .stream()
                .map(this::toImageResponse)
                .toList();
    }

    private RescueOrderImageResponse toImageResponse(
            RescueOrderImage image
    ) {
        return new RescueOrderImageResponse(
                image.getId(),
                image.getImageType(),
                image.getImageUrl(),
                image.getUploadedBy(),
                image.getSortOrder(),
                image.getCreatedAt()
        );
    }

    private RescueOrderResponse toResponse(
            RescueOrder order,
            List<RescueOrderImageResponse> images
    ) {
        return new RescueOrderResponse(
                order.getId(),
                order.getReporterId(),
                order.getIssueType(),
                order.getTitle(),
                order.getDescription(),
                order.getLocationText(),
                order.getLatitude(),
                order.getLongitude(),
                order.getStatus(),
                order.getReviewerId(),
                order.getReviewComment(),
                order.getReviewedAt(),
                order.getPublishedAt(),
                order.getVolunteerId(),
                order.getAcceptedAt(),
                order.getCompletionDescription(),
                order.getCompletionSubmittedAt(),
                order.getConfirmerId(),
                order.getConfirmComment(),
                order.getConfirmedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                images
        );
    }

    private PublicRescueOrderResponse toPublicResponse(
            RescueOrder order
    ) {
        QueryWrapper<RescueOrderImage> imageQuery =
                new QueryWrapper<>();

        imageQuery.eq(
                "rescue_order_id",
                order.getId()
        );

        imageQuery.eq(
                "image_type",
                "REPORT"
        );

        imageQuery.orderByAsc("sort_order");

        List<String> imageUrls =
                rescueOrderImageMapper
                        .selectList(imageQuery)
                        .stream()
                        .map(image ->
                                image.getImageUrl()
                        )
                        .toList();

        return new PublicRescueOrderResponse(
                order.getId(),
                order.getIssueType(),
                order.getTitle(),
                order.getDescription(),
                order.getLocationText(),
                order.getLatitude(),
                order.getLongitude(),
                order.getStatus(),
                order.getPublishedAt(),
                imageUrls
        );
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
