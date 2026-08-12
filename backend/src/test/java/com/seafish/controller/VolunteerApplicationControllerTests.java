package com.seafish.controller;

import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VolunteerApplicationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void requiresAuthenticationToCreateApplication()
            throws Exception {
        mockMvc.perform(
                        post("/api/volunteer-applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validApplicationJson())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void createsPendingApplicationForAuthenticatedUser()
            throws Exception {
        UserResponse user = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(user)
                                .content(validApplicationJson())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.applicantId")
                                .value(user.getId())
                )
                .andExpect(
                        jsonPath("$.data.realName")
                                .value("张三")
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.data.reviewerId")
                                .doesNotExist()
                );
    }

    @Test
    void rejectsInvalidApplicationParameters()
            throws Exception {
        mockMvc.perform(
                        post("/api/volunteer-applications")
                                .with(jwt()
                                        .jwt(token -> token
                                                .subject("1")
                                        )
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_USER"
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "realName": "",
                                          "phone": "123",
                                          "region": "",
                                          "reason": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40001)
                );
    }

    @Test
    @Transactional
    void rejectsSecondPendingApplication()
            throws Exception {
        UserResponse user = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(user)
                                .content(validApplicationJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticatedPost(user)
                                .content(validApplicationJson())
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40906)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("已有待审核的志愿者申请")
                );
    }

    @Test
    void requiresAuthenticationToGetMyApplications()
            throws Exception {
        mockMvc.perform(
                        get("/api/volunteer-applications/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void returnsOnlyCurrentUsersApplications()
            throws Exception {
        UserResponse currentUser = registerTestUser();
        UserResponse anotherUser = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(currentUser)
                                .content(validApplicationJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticatedPost(anotherUser)
                                .content(validApplicationJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/volunteer-applications/me")
                                .with(jwt()
                                        .jwt(token -> token
                                                .subject(
                                                        currentUser
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
                        jsonPath("$.data.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data[0].applicantId")
                                .value(currentUser.getId())
                )
                .andExpect(
                        jsonPath("$.data[0].status")
                                .value("PENDING")
                );
    }

    private MockHttpServletRequestBuilder
            authenticatedPost(UserResponse user) {
        return post("/api/volunteer-applications")
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        user.getId().toString()
                                )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                )
                .contentType(MediaType.APPLICATION_JSON);
    }

    private UserResponse registerTestUser() {
        String username =
                "volunteer_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("志愿者申请测试用户");

        return userService.register(request);
    }

    private String validApplicationJson() {
        return """
                {
                  "realName": "张三",
                  "phone": "13800138000",
                  "region": "山东青岛",
                  "skills": "潜水、海洋动物救助",
                  "reason": "希望参与海洋动物保护"
                }
                """;
    }
}
