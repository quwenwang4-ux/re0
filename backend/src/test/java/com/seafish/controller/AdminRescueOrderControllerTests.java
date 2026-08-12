package com.seafish.controller;

import com.seafish.controller.request.CreateRescueOrderRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.RescueOrderResponse;
import com.seafish.controller.response.UserResponse;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.service.RescueOrderService;
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

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminRescueOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RescueOrderService rescueOrderService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Test
    void requiresAuthenticationToListOrders()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/rescue-orders")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNormalUserWhenListingOrders()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/rescue-orders")
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
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void allowsAdminToListPendingOrders()
            throws Exception {
        UserResponse reporter = registerTestUser();
        RescueOrderResponse order =
                createPendingOrder(reporter);

        mockMvc.perform(
                        adminGet()
                                .param(
                                        "status",
                                        "PENDING_REVIEW"
                                )
                                .param("page", "1")
                                .param("size", "100")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.page")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.records[*].id")
                                .value(
                                        hasItem(
                                                order.getId()
                                                        .intValue()
                                        )
                                )
                );
    }

    @Test
    void rejectsInvalidOrderStatusForAdmin()
            throws Exception {
        mockMvc.perform(
                        adminGet()
                                .param("status", "UNKNOWN")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40012)
                );
    }

    @Test
    @Transactional
    void approvesAndPublishesOrder()
            throws Exception {
        UserResponse reporter = registerTestUser();
        UserResponse admin = registerAdminUser();
        RescueOrderResponse order =
                createPendingOrder(reporter);

        mockMvc.perform(
                        adminReview(order.getId(), admin)
                                .content("""
                                        {
                                          "decision": "APPROVED",
                                          "reviewComment": "信息真实，允许发布"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("OPEN")
                )
                .andExpect(
                        jsonPath("$.data.reviewerId")
                                .value(admin.getId())
                )
                .andExpect(
                        jsonPath("$.data.publishedAt")
                                .isNotEmpty()
                );
    }

    @Test
    @Transactional
    void rejectsOrderWithReviewComment()
            throws Exception {
        UserResponse reporter = registerTestUser();
        UserResponse admin = registerAdminUser();
        RescueOrderResponse order =
                createPendingOrder(reporter);

        mockMvc.perform(
                        adminReview(order.getId(), admin)
                                .content("""
                                        {
                                          "decision": "REJECTED",
                                          "reviewComment": "现场信息不完整"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("REJECTED")
                )
                .andExpect(
                        jsonPath("$.data.reviewComment")
                                .value("现场信息不完整")
                );
    }

    @Test
    @Transactional
    void requiresCommentWhenRejectingOrder()
            throws Exception {
        UserResponse reporter = registerTestUser();
        UserResponse admin = registerAdminUser();
        RescueOrderResponse order =
                createPendingOrder(reporter);

        mockMvc.perform(
                        adminReview(order.getId(), admin)
                                .content("""
                                        {
                                          "decision": "REJECTED"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40011)
                );
    }

    @Test
    @Transactional
    void rejectsRepeatedOrderReview()
            throws Exception {
        UserResponse reporter = registerTestUser();
        UserResponse admin = registerAdminUser();
        RescueOrderResponse order =
                createPendingOrder(reporter);

        mockMvc.perform(
                        adminReview(order.getId(), admin)
                                .content("""
                                        {
                                          "decision": "APPROVED"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        adminReview(order.getId(), admin)
                                .content("""
                                        {
                                          "decision": "REJECTED",
                                          "reviewComment": "再次审核"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40908)
                );
    }

    private MockHttpServletRequestBuilder adminGet() {
        return get("/api/admin/rescue-orders")
                .with(jwt()
                        .jwt(token -> token
                                .subject("999")
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );
    }

    private MockHttpServletRequestBuilder adminReview(
            Long orderId,
            UserResponse admin
    ) {
        return put(
                "/api/admin/rescue-orders/{id}/review",
                orderId
        )
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        admin.getId().toString()
                                )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                )
                .contentType(MediaType.APPLICATION_JSON);
    }

    private RescueOrderResponse createPendingOrder(
            UserResponse reporter
    ) {
        CreateRescueOrderRequest request =
                new CreateRescueOrderRequest();

        request.setIssueType("INJURED_ANIMAL");
        request.setTitle("管理员审核测试工单");
        request.setDescription("发现受伤海洋动物");
        request.setLocationText("青岛市测试海滩");

        return rescueOrderService.createOrder(
                reporter.getId(),
                request
        );
    }

    private UserResponse registerAdminUser() {
        UserResponse admin = registerTestUser();

        int affectedRows =
                sysUserRoleMapper
                        .assignOrReactivateRole(
                                admin.getId(),
                                "ADMIN",
                                null
                        );

        assertTrue(affectedRows >= 1);

        return admin;
    }

    private UserResponse registerTestUser() {
        String username =
                "admin_rescue_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("管理员工单测试用户");

        return userService.register(request);
    }
}
