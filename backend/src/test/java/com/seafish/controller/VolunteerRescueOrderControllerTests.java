package com.seafish.controller;

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

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VolunteerRescueOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RescueOrderService rescueOrderService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Test
    void requiresAuthenticationToSubmitCompletion()
            throws Exception {
        mockMvc.perform(
                        put(
                                "/api/volunteer/rescue-orders/{id}/completion",
                                1
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCompletionJson())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void validatesCompletionDescription()
            throws Exception {
        UserResponse volunteer =
                registerVolunteerUser();

        mockMvc.perform(
                        volunteerCompletion(1L, volunteer)
                                .content("""
                                        {
                                          "completionDescription": "   "
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
    void submitsCompletionWithImages()
            throws Exception {
        RescueOrderResponse openOrder =
                createOpenOrder();
        UserResponse volunteer =
                registerVolunteerUser();

        rescueOrderService.acceptOrder(
                openOrder.getId(),
                volunteer.getId()
        );

        mockMvc.perform(
                        volunteerCompletion(
                                openOrder.getId(),
                                volunteer
                        ).content(validCompletionJson())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("COMPLETION_PENDING")
                )
                .andExpect(
                        jsonPath("$.data.completionDescription")
                                .value("已经完成环境清理和现场检查")
                )
                .andExpect(
                        jsonPath("$.data.completionSubmittedAt")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.data.images[*].imageType")
                                .value(hasItem("COMPLETION"))
                )
                .andExpect(
                        jsonPath("$.data.images[*].imageUrl")
                                .value(hasItem(
                                        "https://example.com/completion-1.jpg"
                                ))
                );
    }

    @Test
    @Transactional
    void rejectsCompletionFromDifferentVolunteer()
            throws Exception {
        RescueOrderResponse openOrder =
                createOpenOrder();
        UserResponse owner =
                registerVolunteerUser();
        UserResponse otherVolunteer =
                registerVolunteerUser();

        rescueOrderService.acceptOrder(
                openOrder.getId(),
                owner.getId()
        );

        mockMvc.perform(
                        volunteerCompletion(
                                openOrder.getId(),
                                otherVolunteer
                        ).content(validCompletionJson())
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40305)
                );
    }

    @Test
    @Transactional
    void rejectsRepeatedCompletionSubmission()
            throws Exception {
        RescueOrderResponse openOrder =
                createOpenOrder();
        UserResponse volunteer =
                registerVolunteerUser();

        rescueOrderService.acceptOrder(
                openOrder.getId(),
                volunteer.getId()
        );

        mockMvc.perform(
                        volunteerCompletion(
                                openOrder.getId(),
                                volunteer
                        ).content(validCompletionJson())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        volunteerCompletion(
                                openOrder.getId(),
                                volunteer
                        ).content(validCompletionJson())
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40910)
                );
    }

    @Test
    void requiresAuthenticationToGetMyOrders()
            throws Exception {
        mockMvc.perform(
                        get("/api/volunteer/rescue-orders/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNormalUserWhenGettingMyOrders()
            throws Exception {
        mockMvc.perform(
                        get("/api/volunteer/rescue-orders/me")
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
    void returnsOnlyCurrentVolunteersOrders()
            throws Exception {
        RescueOrderResponse firstOpenOrder =
                createOpenOrder();
        RescueOrderResponse secondOpenOrder =
                createOpenOrder();
        UserResponse firstVolunteer =
                registerVolunteerUser();
        UserResponse secondVolunteer =
                registerVolunteerUser();

        rescueOrderService.acceptOrder(
                firstOpenOrder.getId(),
                firstVolunteer.getId()
        );
        rescueOrderService.acceptOrder(
                secondOpenOrder.getId(),
                secondVolunteer.getId()
        );

        mockMvc.perform(
                        volunteerGet(firstVolunteer)
                                .param("page", "1")
                                .param("size", "100")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.records[*].id")
                                .value(hasItem(
                                        firstOpenOrder.getId()
                                                .intValue()
                                ))
                )
                .andExpect(
                        jsonPath("$.data.records[*].id")
                                .value(not(hasItem(
                                        secondOpenOrder.getId()
                                                .intValue()
                                )))
                );
    }

    @Test
    @Transactional
    void rejectsInvalidPageWhenGettingMyOrders()
            throws Exception {
        UserResponse volunteer =
                registerVolunteerUser();

        mockMvc.perform(
                        volunteerGet(volunteer)
                                .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40005)
                );
    }

    @Test
    void requiresAuthenticationToAcceptOrder()
            throws Exception {
        mockMvc.perform(
                        put(
                                "/api/volunteer/rescue-orders/{id}/accept",
                                1
                        )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNormalUserAtControllerLayer()
            throws Exception {
        mockMvc.perform(
                        put(
                                "/api/volunteer/rescue-orders/{id}/accept",
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
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void rejectsUserWithoutDatabaseVolunteerRole()
            throws Exception {
        UserResponse user = registerTestUser();

        mockMvc.perform(
                        volunteerAccept(1L, user)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40304)
                );
    }

    @Test
    @Transactional
    void allowsVolunteerToAcceptOpenOrder()
            throws Exception {
        RescueOrderResponse openOrder =
                createOpenOrder();
        UserResponse volunteer =
                registerVolunteerUser();

        mockMvc.perform(
                        volunteerAccept(
                                openOrder.getId(),
                                volunteer
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("ACCEPTED")
                )
                .andExpect(
                        jsonPath("$.data.volunteerId")
                                .value(volunteer.getId())
                )
                .andExpect(
                        jsonPath("$.data.acceptedAt")
                                .isNotEmpty()
                );
    }

    @Test
    @Transactional
    void rejectsPendingOrderAcceptance()
            throws Exception {
        UserResponse reporter = registerTestUser();
        RescueOrderResponse pendingOrder =
                createPendingOrder(reporter);
        UserResponse volunteer =
                registerVolunteerUser();

        mockMvc.perform(
                        volunteerAccept(
                                pendingOrder.getId(),
                                volunteer
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40909)
                );
    }

    @Test
    @Transactional
    void preventsSecondVolunteerFromAcceptingSameOrder()
            throws Exception {
        RescueOrderResponse openOrder =
                createOpenOrder();
        UserResponse firstVolunteer =
                registerVolunteerUser();
        UserResponse secondVolunteer =
                registerVolunteerUser();

        mockMvc.perform(
                        volunteerAccept(
                                openOrder.getId(),
                                firstVolunteer
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        volunteerAccept(
                                openOrder.getId(),
                                secondVolunteer
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40909)
                );
    }

    private MockHttpServletRequestBuilder volunteerAccept(
            Long orderId,
            UserResponse volunteer
    ) {
        return put(
                "/api/volunteer/rescue-orders/{id}/accept",
                orderId
        )
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        volunteer.getId().toString()
                                )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_VOLUNTEER"
                                )
                        )
                );
    }

    private MockHttpServletRequestBuilder volunteerGet(
            UserResponse volunteer
    ) {
        return get("/api/volunteer/rescue-orders/me")
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        volunteer.getId().toString()
                                )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_VOLUNTEER"
                                )
                        )
                );
    }

    private MockHttpServletRequestBuilder volunteerCompletion(
            Long orderId,
            UserResponse volunteer
    ) {
        return put(
                "/api/volunteer/rescue-orders/{id}/completion",
                orderId
        )
                .with(jwt()
                        .jwt(token -> token
                                .subject(
                                        volunteer.getId().toString()
                                )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_VOLUNTEER"
                                )
                        )
                )
                .contentType(MediaType.APPLICATION_JSON);
    }

    private String validCompletionJson() {
        return """
                {
                  "completionDescription": "已经完成环境清理和现场检查",
                  "imageUrls": [
                    "https://example.com/completion-1.jpg",
                    "https://example.com/completion-2.jpg"
                  ]
                }
                """;
    }

    private RescueOrderResponse createOpenOrder() {
        UserResponse reporter = registerTestUser();
        RescueOrderResponse pendingOrder =
                createPendingOrder(reporter);
        UserResponse admin = registerRoleUser("ADMIN");

        ReviewRescueOrderRequest request =
                new ReviewRescueOrderRequest();
        request.setDecision("APPROVED");
        request.setReviewComment("允许发布并由志愿者接单");

        return rescueOrderService.reviewOrder(
                pendingOrder.getId(),
                admin.getId(),
                request
        );
    }

    private RescueOrderResponse createPendingOrder(
            UserResponse reporter
    ) {
        CreateRescueOrderRequest request =
                new CreateRescueOrderRequest();

        request.setIssueType("ENVIRONMENT");
        request.setTitle("志愿者接单测试工单");
        request.setDescription("海滩存在需要处理的环境问题");
        request.setLocationText("测试海滩区域");

        return rescueOrderService.createOrder(
                reporter.getId(),
                request
        );
    }

    private UserResponse registerVolunteerUser() {
        return registerRoleUser("VOLUNTEER");
    }

    private UserResponse registerRoleUser(
            String roleCode
    ) {
        UserResponse user = registerTestUser();

        int affectedRows =
                sysUserRoleMapper
                        .assignOrReactivateRole(
                                user.getId(),
                                roleCode,
                                null
                        );

        assertTrue(affectedRows >= 1);
        return user;
    }

    private UserResponse registerTestUser() {
        String username =
                "accept_order_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("接单测试用户");

        return userService.register(request);
    }
}
