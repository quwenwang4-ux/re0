package com.seafish.service;

import com.seafish.controller.request.LoginRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.LoginResponse;
import com.seafish.controller.response.UserResponse;
import com.seafish.entity.SysUser;
import com.seafish.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void registersUserWithPasswordHashAndRole() {
        String username =
                "test_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        String originalPassword =
                "Ocean1234";

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword(originalPassword);
        request.setNickname("注册测试用户");

        UserResponse response =
                userService.register(request);

        assertNotNull(response.getId());
        assertEquals(
                username,
                response.getUsername()
        );

        SysUser savedUser =
                sysUserMapper.selectById(
                        response.getId()
                );

        assertNotNull(savedUser);

        assertNotEquals(
                originalPassword,
                savedUser.getPasswordHash()
        );

        assertTrue(
                passwordEncoder.matches(
                        originalPassword,
                        savedUser.getPasswordHash()
                )
        );

        Integer roleCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM sys_user_role ur
                        JOIN sys_role r
                          ON r.id = ur.role_id
                        WHERE ur.user_id = ?
                          AND r.role_code = 'USER'
                        """,
                        Integer.class,
                        response.getId()
                );

        assertNotNull(roleCount);
        assertEquals(
                1,
                roleCount.intValue()
        );
    }

    @Test
    @Transactional
    void logsInRegisteredUser() {
        String username =
                "login_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        String password = "Ocean1234";

        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setNickname("登录测试用户");

        userService.register(registerRequest);

        LoginRequest loginRequest =
                new LoginRequest();

        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        LoginResponse response =
                userService.login(loginRequest);

        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(
                username,
                response.getUser().getUsername()
        );
        assertTrue(response.getRoles().contains("USER"));
    }
}
