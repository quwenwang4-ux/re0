package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.LoginRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.CurrentUserResponse;
import com.seafish.controller.response.LoginResponse;
import com.seafish.controller.response.UserResponse;
import com.seafish.entity.SysUser;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.SysUserMapper;
import com.seafish.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seafish.controller.request.UpdateProfileRequest;
import com.seafish.common.PageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

@Service
public class UserService {


    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final TokenService tokenService;

    public UserService(
            SysUserMapper sysUserMapper,
            SysUserRoleMapper sysUserRoleMapper,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }


    @Transactional
    public UserResponse register(
            RegisterRequest request
    ) {
        String username =
                request.getUsername().trim();

        String email =
                normalizeOptional(request.getEmail());

        String phone =
                normalizeOptional(request.getPhone());

        QueryWrapper<SysUser> duplicateQuery =
                new QueryWrapper<>();

        duplicateQuery.eq(
                "username",
                username
        );

        if (email != null) {
            duplicateQuery
                    .or()
                    .eq("email", email);
        }

        if (phone != null) {
            duplicateQuery
                    .or()
                    .eq("phone", phone);
        }

        Long duplicateCount =
                sysUserMapper.selectCount(
                        duplicateQuery
                );

        if (duplicateCount > 0) {
            throw new BusinessException(
                    40902,
                    "用户名、邮箱或手机号已被使用"
            );
        }

        SysUser user = new SysUser();

        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );
        user.setNickname(
                normalizeOptional(request.getNickname())
        );
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus("ACTIVE");

        int insertedRows =
                sysUserMapper.insert(user);

        if (insertedRows != 1) {
            throw new BusinessException(
                    50004,
                    "用户注册失败"
            );
        }

        int assignedRoles =
                sysUserRoleMapper.assignRole(
                        user.getId(),
                        "USER"
                );

        if (assignedRoles != 1) {
            throw new BusinessException(
                    50005,
                    "普通用户角色分配失败"
            );
        }

        SysUser savedUser =
                sysUserMapper.selectById(
                        user.getId()
                );

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(
            LoginRequest request
    ) {
        String username =
                request.getUsername().trim();

        QueryWrapper<SysUser> userQuery =
                new QueryWrapper<>();

        userQuery.eq("username", username);

        SysUser user =
                sysUserMapper.selectOne(userQuery);

        if (user == null
                || !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                )) {
            throw new BusinessException(
                    40101,
                    "用户名或密码错误"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(
                    40301,
                    "账号已被停用"
            );
        }

        List<String> roles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                user.getId()
                        );

        if (roles.isEmpty()) {
            throw new BusinessException(
                    40302,
                    "账号没有可用角色"
            );
        }

        String accessToken =
                tokenService.createAccessToken(
                        user,
                        roles
                );

        return new LoginResponse(
                accessToken,
                "Bearer",
                tokenService.getExpirationSeconds(),
                toResponse(user),
                roles
        );

    }


    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(
            Long userId
    ) {
        SysUser user =
                sysUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(
                    40402,
                    "用户不存在"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(
                    40301,
                    "账号已被停用"
            );
        }

        List<String> roles =
                sysUserRoleMapper
                        .selectActiveRoleCodes(userId);

        return new CurrentUserResponse(
                toResponse(user),
                roles
        );
    }

    @Transactional
    public UserResponse updateCurrentUser(
            Long userId,
            UpdateProfileRequest request
    ) {
        SysUser user =
                sysUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(
                    40402,
                    "用户不存在"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(
                    40301,
                    "账号已被停用"
            );
        }

        String email =
                normalizeOptional(request.getEmail());

        String phone =
                normalizeOptional(request.getPhone());

        if (email != null) {
            QueryWrapper<SysUser> emailQuery =
                    new QueryWrapper<>();

            emailQuery
                    .eq("email", email)
                    .ne("id", userId);

            Long emailCount =
                    sysUserMapper.selectCount(emailQuery);

            if (emailCount > 0) {
                throw new BusinessException(
                        40903,
                        "邮箱已被其他用户使用"
                );
            }
        }

        if (phone != null) {
            QueryWrapper<SysUser> phoneQuery =
                    new QueryWrapper<>();

            phoneQuery
                    .eq("phone", phone)
                    .ne("id", userId);

            Long phoneCount =
                    sysUserMapper.selectCount(phoneQuery);

            if (phoneCount > 0) {
                throw new BusinessException(
                        40904,
                        "手机号已被其他用户使用"
                );
            }
        }

        if (request.getNickname() != null) {
            user.setNickname(
                    normalizeOptional(request.getNickname())
            );
        }

        if (request.getEmail() != null) {
            user.setEmail(email);
        }

        if (request.getPhone() != null) {
            user.setPhone(phone);
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(
                    normalizeOptional(request.getAvatarUrl())
            );
        }

        int updatedRows =
                sysUserMapper.updateById(user);

        if (updatedRows != 1) {
            throw new BusinessException(
                    50006,
                    "个人资料修改失败"
            );
        }

        SysUser updatedUser =
                sysUserMapper.selectById(userId);

        return toResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(
            Long operatorId,
            String keyword,
            String status,
            long page,
            long size
    ) {
        requireAdmin(operatorId);

        if (page < 1) {
            throw new BusinessException(40005, "页码不能小于1");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(40006, "每页数量必须在1到100之间");
        }

        QueryWrapper<SysUser> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String normalized = keyword.trim();
            query.and(wrapper -> wrapper
                    .like("username", normalized)
                    .or()
                    .like("nickname", normalized)
                    .or()
                    .like("email", normalized)
                    .or()
                    .like("phone", normalized));
        }
        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.trim().toUpperCase();
            if (!List.of("ACTIVE", "DISABLED").contains(normalizedStatus)) {
                throw new BusinessException(40040, "用户状态不合法");
            }
            query.eq("status", normalizedStatus);
        }
        query.orderByDesc("created_at").orderByDesc("id");

        Page<SysUser> result = sysUserMapper.selectPage(
                new Page<>(page, size),
                query
        );
        return new PageResponse<>(
                result.getRecords().stream()
                        .map(this::toResponse)
                        .toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages()
        );
    }

    @Transactional
    public UserResponse updateUserStatus(
            Long targetUserId,
            Long operatorId,
            String status
    ) {
        requireAdmin(operatorId);

        if (targetUserId.equals(operatorId)) {
            throw new BusinessException(40041, "管理员不能停用自己的账号");
        }

        SysUser target = sysUserMapper.selectById(targetUserId);
        if (target == null) {
            throw new BusinessException(40402, "用户不存在");
        }

        String normalizedStatus = status.trim().toUpperCase();
        target.setStatus(normalizedStatus);
        if (sysUserMapper.updateById(target) != 1) {
            throw new BusinessException(50042, "用户状态修改失败");
        }
        return toResponse(sysUserMapper.selectById(targetUserId));
    }

    private void requireAdmin(Long operatorId) {
        SysUser operator = sysUserMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(40402, "管理员用户不存在");
        }
        if (!"ACTIVE".equals(operator.getStatus())) {
            throw new BusinessException(40301, "管理员账号已被停用");
        }
        if (!sysUserRoleMapper.selectActiveRoleCodes(operatorId)
                .contains("ADMIN")) {
            throw new BusinessException(40303, "当前用户没有管理员权限");
        }
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private UserResponse toResponse(
            SysUser user
    ) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
