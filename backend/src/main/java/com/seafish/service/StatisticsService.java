package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.response.AdminStatisticsResponse;
import com.seafish.controller.response.LabelCountResponse;
import com.seafish.entity.DetectionRecord;
import com.seafish.entity.DetectionResult;
import com.seafish.entity.RescueOrder;
import com.seafish.entity.SysUser;
import com.seafish.mapper.DetectionRecordMapper;
import com.seafish.mapper.DetectionResultMapper;
import com.seafish.mapper.RescueOrderMapper;
import com.seafish.mapper.SysUserMapper;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final DetectionRecordMapper detectionRecordMapper;
    private final DetectionResultMapper detectionResultMapper;
    private final RescueOrderMapper rescueOrderMapper;

    public StatisticsService(
            SysUserMapper userMapper,
            SysUserRoleMapper userRoleMapper,
            DetectionRecordMapper detectionRecordMapper,
            DetectionResultMapper detectionResultMapper,
            RescueOrderMapper rescueOrderMapper
    ) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.detectionRecordMapper = detectionRecordMapper;
        this.detectionResultMapper = detectionResultMapper;
        this.rescueOrderMapper = rescueOrderMapper;
    }

    @Transactional(readOnly = true)
    public AdminStatisticsResponse getOverview(Long operatorId) {
        requireAdmin(operatorId);

        QueryWrapper<SysUser> activeUsers = new QueryWrapper<>();
        activeUsers.eq("status", "ACTIVE");

        QueryWrapper<RescueOrder> completedOrders = new QueryWrapper<>();
        completedOrders.eq("status", "COMPLETED");

        return new AdminStatisticsResponse(
                userMapper.selectCount(activeUsers),
                detectionRecordMapper.selectCount(null),
                rescueOrderMapper.selectCount(null),
                rescueOrderMapper.selectCount(completedOrders),
                fromRawMaps(userRoleMapper.countUsersByRole()),
                countDetectionStatuses(),
                countTopClasses(),
                countRescueStatuses(),
                countVolunteerOrders(false),
                countVolunteerOrders(true)
        );
    }

    private void requireAdmin(Long operatorId) {
        SysUser operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(40402, "管理员用户不存在");
        }
        if (!"ACTIVE".equals(operator.getStatus())) {
            throw new BusinessException(40301, "管理员账号已被停用");
        }
        if (!userRoleMapper.selectActiveRoleCodes(operatorId)
                .contains("ADMIN")) {
            throw new BusinessException(40303, "当前用户没有管理员权限");
        }
    }

    private List<LabelCountResponse> countDetectionStatuses() {
        List<DetectionRecord> records =
                detectionRecordMapper.selectList(null);
        return countLabels(
                records.stream()
                        .map(DetectionRecord::getStatus)
                        .toList(),
                0
        );
    }

    private List<LabelCountResponse> countTopClasses() {
        List<DetectionResult> results =
                detectionResultMapper.selectList(null);
        return countLabels(
                results.stream()
                        .map(DetectionResult::getClassName)
                        .toList(),
                10
        );
    }

    private List<LabelCountResponse> countRescueStatuses() {
        List<RescueOrder> orders = rescueOrderMapper.selectList(null);
        return countLabels(
                orders.stream()
                        .map(RescueOrder::getStatus)
                        .toList(),
                0
        );
    }

    private List<LabelCountResponse> countVolunteerOrders(
            boolean completedOnly
    ) {
        QueryWrapper<RescueOrder> query = new QueryWrapper<>();
        query.isNotNull("volunteer_id");
        if (completedOnly) {
            query.eq("status", "COMPLETED");
        }

        List<RescueOrder> orders = rescueOrderMapper.selectList(query);
        List<String> volunteerIds = orders.stream()
                .map(order -> String.valueOf(order.getVolunteerId()))
                .toList();
        return countLabels(volunteerIds, 10);
    }

    private List<LabelCountResponse> countLabels(
            List<String> labels,
            int limit
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String label : labels) {
            if (label != null) {
                counts.merge(label, 1L, Long::sum);
            }
        }

        return counts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                        .reversed())
                .limit(limit <= 0 ? Long.MAX_VALUE : limit)
                .map(entry -> new LabelCountResponse(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    private List<LabelCountResponse> fromRawMaps(
            List<Map<String, Object>> rows
    ) {
        List<LabelCountResponse> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object label = row.get("label");
            Object value = row.get("value");
            result.add(new LabelCountResponse(
                    String.valueOf(label),
                    value instanceof Number number
                            ? number.longValue()
                            : Long.parseLong(String.valueOf(value))
            ));
        }
        return result;
    }
}
