package com.seafish.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.entity.SysUser;
import com.seafish.exception.BusinessException;
import com.seafish.mapper.SysUserMapper;
import com.seafish.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {


    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper sysUserRoleMapper;

    public UserService(
            SysUserMapper sysUserMapper,
            SysUserRoleMapper sysUserRoleMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
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