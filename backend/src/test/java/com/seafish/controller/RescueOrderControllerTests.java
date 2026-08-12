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
class RescueOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void requiresAuthenticationToCreateOrder()
            throws Exception {
        mockMvc.perform(
                        post("/api/rescue-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validOrderJson())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void createsPendingOrderWithReportImages()
            throws Exception {
        UserResponse reporter = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(reporter)
                                .content(validOrderJson())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.reporterId")
                                .value(reporter.getId())
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("PENDING_REVIEW")
                )
                .andExpect(
                        jsonPath("$.data.images.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.data.images[0].imageType")
                                .value("REPORT")
                )
                .andExpect(
                        jsonPath("$.data.images[0].sortOrder")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.data.images[1].sortOrder")
                                .value(1)
                );
    }

    @Test
    @Transactional
    void requiresLocationWhenCreatingOrder()
            throws Exception {
        UserResponse reporter = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(reporter)
                                .content("""
                                        {
                                          "issueType": "ENVIRONMENT",
                                          "title": "海滩塑料垃圾",
                                          "description": "发现大量塑料垃圾"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40009)
                );
    }

    @Test
    @Transactional
    void requiresLatitudeAndLongitudeTogether()
            throws Exception {
        UserResponse reporter = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(reporter)
                                .content("""
                                        {
                                          "issueType": "INJURED_ANIMAL",
                                          "title": "发现受伤海龟",
                                          "description": "海龟鳍部受伤",
                                          "latitude": 36.1234567
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40010)
                );
    }

    @Test
    void rejectsInvalidIssueType()
            throws Exception {
        mockMvc.perform(
                        post("/api/rescue-orders")
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
                                          "issueType": "INVALID",
                                          "title": "测试工单",
                                          "description": "测试描述",
                                          "locationText": "测试位置"
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
    void requiresAuthenticationToGetMyOrders()
            throws Exception {
        mockMvc.perform(
                        get("/api/rescue-orders/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void returnsOnlyCurrentUsersOrders()
            throws Exception {
        UserResponse currentUser = registerTestUser();
        UserResponse anotherUser = registerTestUser();

        mockMvc.perform(
                        authenticatedPost(currentUser)
                                .content(validOrderJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticatedPost(anotherUser)
                                .content(validOrderJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticatedGet(currentUser)
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.total")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.records.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.records[0].reporterId")
                                .value(currentUser.getId())
                )
                .andExpect(
                        jsonPath("$.data.records[0].images.length()")
                                .value(2)
                );
    }

    @Test
    @Transactional
    void rejectsInvalidPageWhenGettingMyOrders()
            throws Exception {
        UserResponse reporter = registerTestUser();

        mockMvc.perform(
                        authenticatedGet(reporter)
                                .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40005)
                );
    }

    private MockHttpServletRequestBuilder
            authenticatedPost(UserResponse reporter) {
        return post("/api/rescue-orders")
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        reporter
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
                .contentType(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder
            authenticatedGet(UserResponse reporter) {
        return get("/api/rescue-orders/me")
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        reporter
                                                .getId()
                                                .toString()
                                )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );
    }

    private UserResponse registerTestUser() {
        String username =
                "rescue_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("救助工单测试用户");

        return userService.register(request);
    }

    private String validOrderJson() {
        return """
                {
                  "issueType": "INJURED_ANIMAL",
                  "title": "海滩发现受伤海龟",
                  "description": "海龟左侧鳍部受伤，目前仍有活动迹象",
                  "locationText": "青岛市崂山区某海滩",
                  "imageUrls": [
                    "/uploads/rescue/turtle-01.jpg",
                    "/uploads/rescue/turtle-02.jpg"
                  ]
                }
                """;
    }
}
