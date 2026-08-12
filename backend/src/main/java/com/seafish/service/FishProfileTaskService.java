package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seafish.common.PageResponse;
import com.seafish.controller.request.ReviewFishProfileTaskRequest;
import com.seafish.entity.DetectionResult;
import com.seafish.entity.FishInfo;
import com.seafish.entity.FishProfileTask;
import com.seafish.entity.SysUser;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.DetectionResultMapper;
import com.seafish.mapper.FishInfoMapper;
import com.seafish.mapper.FishProfileTaskMapper;
import com.seafish.mapper.SysUserMapper;
import com.seafish.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FishProfileTaskService {

    private final FishProfileTaskMapper taskMapper;
    private final DetectionResultMapper resultMapper;
    private final FishInfoMapper fishInfoMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    public FishProfileTaskService(
            FishProfileTaskMapper taskMapper,
            DetectionResultMapper resultMapper,
            FishInfoMapper fishInfoMapper,
            SysUserMapper userMapper,
            SysUserRoleMapper userRoleMapper
    ) {
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.fishInfoMapper = fishInfoMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FishProfileTask> list(
            String status,
            long page,
            long size
    ) {
        validatePage(page, size);
        QueryWrapper<FishProfileTask> query = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            String normalized = status.trim().toUpperCase();
            if (!List.of("PENDING", "RESOLVED", "IGNORED")
                    .contains(normalized)) {
                throw new BusinessException(40034, "补全任务状态不合法");
            }
            query.eq("status", normalized);
        }
        query.orderByDesc("created_at").orderByDesc("id");
        Page<FishProfileTask> result = taskMapper.selectPage(
                new Page<>(page, size),
                query
        );
        return new PageResponse<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages()
        );
    }

    @Transactional
    public FishProfileTask review(
            Long taskId,
            Long reviewerId,
            ReviewFishProfileTaskRequest request
    ) {
        requireAdmin(reviewerId);
        FishProfileTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(40441, "鱼类资料补全任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(40922, "补全任务已处理");
        }

        String decision = request.getDecision().trim().toUpperCase();
        String comment = request.getReviewComment() == null
                || request.getReviewComment().isBlank()
                ? null
                : request.getReviewComment().trim();

        Long linkedFishId = null;
        if ("RESOLVED".equals(decision)) {
            if (request.getFishId() == null) {
                throw new BusinessException(
                        40035,
                        "完成补全任务时必须选择鱼类资料"
                );
            }
            FishInfo fish = fishInfoMapper.selectById(request.getFishId());
            if (fish == null) {
                throw new BusinessException(40401, "鱼类信息不存在");
            }
            linkedFishId = fish.getId();

            DetectionResult result = resultMapper.selectById(
                    task.getDetectionResultId()
            );
            if (result == null) {
                throw new BusinessException(40442, "识别结果不存在");
            }
            result.setFishId(linkedFishId);
            if (resultMapper.updateById(result) != 1) {
                throw new BusinessException(50034, "识别结果关联失败");
            }
        } else if (comment == null) {
            throw new BusinessException(
                    40036,
                    "忽略补全任务时必须填写处理意见"
            );
        }

        UpdateWrapper<FishProfileTask> update = new UpdateWrapper<>();
        update.eq("id", taskId)
                .eq("status", "PENDING")
                .set("status", decision)
                .set("linked_fish_id", linkedFishId)
                .set("reviewer_id", reviewerId)
                .set("review_comment", comment)
                .set("reviewed_at", LocalDateTime.now());

        if (taskMapper.update(null, update) != 1) {
            throw new BusinessException(40922, "补全任务已处理");
        }
        return taskMapper.selectById(taskId);
    }

    private void requireAdmin(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40402, "用户不存在");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(40301, "用户账号已被停用");
        }
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
}
