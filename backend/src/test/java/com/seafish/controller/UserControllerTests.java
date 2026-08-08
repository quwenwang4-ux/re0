package com.seafish.controller;

import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void requiresAuthenticationToGetMe()
            throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void returnsCurrentUserForValidToken()
            throws Exception {
        String username =
                "me_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("个人中心测试用户");

        UserResponse registeredUser =
                userService.register(request);

        mockMvc.perform(
                        get("/api/users/me")
                                .with(jwt()
                                        .jwt(token -> token
                                                .subject(
                                                        registeredUser
                                                                .getId()
                                                                .toString()
                                                )
                                        )
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_USER"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.user.username")
                                .value(username)
                )
                .andExpect(
                        jsonPath("$.data.roles[0]")
                                .value("USER")
                );
    }
}
