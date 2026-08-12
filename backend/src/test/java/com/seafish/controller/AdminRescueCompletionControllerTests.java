package com.seafish.controller;

import com.seafish.controller.request.CompleteRescueOrderRequest;
import com.seafish.controller.request.ConfirmRescueCompletionRequest;
import com.seafish.controller.request.CreateRescueOrderRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.request.ReviewRescueOrderRequest;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminRescueCompletionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RescueOrderService rescueOrderService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Test
    void requiresAuthenticationToConfirmCompletion()
            throws Exception {
        mockMvc.perform(
                        put(
                                "/api/admin/rescue-orders/{id}/completion-confirmation",
                                1
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(confirmedJson())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNormalUserAtControllerLayer()
            throws Exception {
        mockMvc.perform(
                        put(
                                "/api/admin/rescue-orders/{id}/completion-confirmation",
                                1
                        )
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
                                .content(confirmedJson())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void rejectsTokenAdminWithoutDatabaseAdminRole()
            throws Exception {
        CompletionScenario scenario =
                createCompletionPendingOrder();
        UserResponse normalUser = registerTestUser();

        mockMvc.perform(
                        adminConfirmation(
                                scenario.order().getId(),
                                normalUser
                        ).content(confirmedJson())
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40303)
                );
    }

    @Test
    @Transactional
    void confirmsCompletionSuccessfully()
            throws Exception {
        CompletionScenario scenario =
                createCompletionPendingOrder();
        UserResponse confirmer =
                registerRoleUser("ADMIN");

        mockMvc.perform(
                        adminConfirmation(
                                scenario.order().getId(),
                                confirmer
                        ).content(confirmedJson())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("COMPLETED")
                )
                .andExpect(
                        jsonPath("$.data.confirmerId")
                                .value(confirmer.getId())
                )
                .andExpect(
                        jsonPath("$.data.confirmComment")
                                .value("完成情况符合要求")
                )
                .andExpect(
                        jsonPath("$.data.confirmedAt")
                                .isNotEmpty()
                );
    }

    @Test
    @Transactional
    void requiresCommentWhenRejectingCompletion()
            throws Exception {
        CompletionScenario scenario =
                createCompletionPendingOrder();
        UserResponse confirmer =
                registerRoleUser("ADMIN");

        mockMvc.perform(
                        adminConfirmation(
                                scenario.order().getId(),
                                confirmer
                        ).content("""
                                {
                                  "decision": "REJECTED"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40013)
                );
    }

    @Test
    @Transactional
    void rejectsCompletionAndAllowsResubmission()
            throws Exception {
        CompletionScenario scenario =
                createCompletionPendingOrder();
        UserResponse confirmer =
                registerRoleUser("ADMIN");

        mockMvc.perform(
                        adminConfirmation(
                                scenario.order().getId(),
                                confirmer
                        ).content("""
                                {
                                  "decision": "REJECTED",
                                  "confirmComment": "现场照片不够清晰，请重新提交"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("ACCEPTED")
                )
                .andExpect(
                        jsonPath("$.data.completionDescription")
                                .value(nullValue())
                )
                .andExpect(
                        jsonPath("$.data.images[*].imageType")
                                .value(everyItem(not("COMPLETION")))
                );

        CompleteRescueOrderRequest retryRequest =
                new CompleteRescueOrderRequest();
        retryRequest.setCompletionDescription(
                "根据要求重新处理并补充清晰照片"
        );
        retryRequest.setImageUrls(List.of(
                "https://example.com/completion-retry.jpg"
        ));

        RescueOrderResponse retried =
                rescueOrderService.submitCompletion(
                        scenario.order().getId(),
                        scenario.volunteer().getId(),
                        retryRequest
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "COMPLETION_PENDING",
                retried.getStatus()
        );
        org.junit.jupiter.api.Assertions.assertNull(
                retried.getConfirmerId()
        );
    }

    @Test
    @Transactional
    void rejectsRepeatedCompletionConfirmation()
            throws Exception {
        CompletionScenario scenario =
                createCompletionPendingOrder();
        UserResponse confirmer =
                registerRoleUser("ADMIN");

        mockMvc.perform(
                        adminConfirmation(
                                scenario.order().getId(),
                                confirmer
                        ).content(confirmedJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        adminConfirmation(
                                scenario.order().getId(),
                                confirmer
                        ).content(confirmedJson())
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40911)
                );
    }

    private MockHttpServletRequestBuilder adminConfirmation(
            Long orderId,
            UserResponse admin
    ) {
        return put(
                "/api/admin/rescue-orders/{id}/completion-confirmation",
                orderId
        )
                .with(jwt()
                        .jwt(token -> token
                                .subject(admin.getId().toString())
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                )
                .contentType(MediaType.APPLICATION_JSON);
    }

    private CompletionScenario createCompletionPendingOrder() {
        UserResponse reporter = registerTestUser();
        UserResponse reviewer = registerRoleUser("ADMIN");
        UserResponse volunteer = registerRoleUser("VOLUNTEER");

        CreateRescueOrderRequest createRequest =
                new CreateRescueOrderRequest();
        createRequest.setIssueType("ENVIRONMENT");
        createRequest.setTitle("管理员终审测试工单");
        createRequest.setDescription("需要完成处理并提交反馈");
        createRequest.setLocationText("测试海滩");

        RescueOrderResponse pending =
                rescueOrderService.createOrder(
                        reporter.getId(),
                        createRequest
                );

        ReviewRescueOrderRequest reviewRequest =
                new ReviewRescueOrderRequest();
        reviewRequest.setDecision("APPROVED");

        rescueOrderService.reviewOrder(
                pending.getId(),
                reviewer.getId(),
                reviewRequest
        );
        rescueOrderService.acceptOrder(
                pending.getId(),
                volunteer.getId()
        );

        CompleteRescueOrderRequest completeRequest =
                new CompleteRescueOrderRequest();
        completeRequest.setCompletionDescription(
                "已完成现场处理"
        );
        completeRequest.setImageUrls(List.of(
                "https://example.com/completion-final.jpg"
        ));

        RescueOrderResponse completionPending =
                rescueOrderService.submitCompletion(
                        pending.getId(),
                        volunteer.getId(),
                        completeRequest
                );

        return new CompletionScenario(
                completionPending,
                volunteer
        );
    }

    private String confirmedJson() {
        return """
                {
                  "decision": "CONFIRMED",
                  "confirmComment": "完成情况符合要求"
                }
                """;
    }

    private UserResponse registerRoleUser(String roleCode) {
        UserResponse user = registerTestUser();
        int affectedRows =
                sysUserRoleMapper.assignOrReactivateRole(
                        user.getId(),
                        roleCode,
                        null
                );
        assertTrue(affectedRows >= 1);
        return user;
    }

    private UserResponse registerTestUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(
                "confirm_rescue_" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
        );
        request.setPassword("Ocean1234");
        request.setNickname("工单终审测试用户");
        return userService.register(request);
    }

    private record CompletionScenario(
            RescueOrderResponse order,
            UserResponse volunteer
    ) {
    }
}
