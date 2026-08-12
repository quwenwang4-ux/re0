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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicRescueOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RescueOrderService rescueOrderService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Test
    void allowsGuestToListPublicOrders()
            throws Exception {
        mockMvc.perform(
                        get("/api/rescue-orders/public")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.page")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.size")
                                .value(10)
                );
    }

    @Test
    @Transactional
    void doesNotExposePendingOrders()
            throws Exception {
        UserResponse reporter = registerTestUser();
        RescueOrderResponse pendingOrder =
                createPendingOrder(reporter);

        mockMvc.perform(
                        get("/api/rescue-orders/public")
                                .param("page", "1")
                                .param("size", "100")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.records[*].id")
                                .value(not(hasItem(
                                        pendingOrder.getId()
                                                .intValue()
                                )))
                );
    }

    @Test
    @Transactional
    void exposesApprovedOrderWithoutInternalUserFields()
            throws Exception {
        UserResponse reporter = registerTestUser();
        UserResponse admin = registerAdminUser();
        RescueOrderResponse pendingOrder =
                createPendingOrder(reporter);

        ReviewRescueOrderRequest reviewRequest =
                new ReviewRescueOrderRequest();
        reviewRequest.setDecision("APPROVED");
        reviewRequest.setReviewComment(
                "测试审核通过"
        );

        rescueOrderService.reviewOrder(
                pendingOrder.getId(),
                admin.getId(),
                reviewRequest
        );

        mockMvc.perform(
                        get("/api/rescue-orders/public")
                                .param("page", "1")
                                .param("size", "100")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.records[*].id")
                                .value(hasItem(
                                        pendingOrder.getId()
                                                .intValue()
                                ))
                )
                .andExpect(
                        jsonPath(
                                "$.data.records[*].imageUrls[*]"
                        ).value(hasItem(
                                "https://example.com/rescue-1.jpg"
                        ))
                )
                .andExpect(
                        jsonPath(
                                "$.data.records[*].reporterId"
                        ).doesNotExist()
                )
                .andExpect(
                        jsonPath(
                                "$.data.records[*].reviewerId"
                        ).doesNotExist()
                )
                .andExpect(
                        jsonPath(
                                "$.data.records[*].reviewComment"
                        ).doesNotExist()
                );
    }

    @Test
    @Transactional
    void keepsAcceptedOrderVisibleForPublicProgressTracking()
            throws Exception {
        UserResponse reporter = registerTestUser();
        UserResponse admin = registerAdminUser();
        UserResponse volunteer = registerRoleUser(
                "VOLUNTEER"
        );
        RescueOrderResponse pendingOrder =
                createPendingOrder(reporter);

        ReviewRescueOrderRequest reviewRequest =
                new ReviewRescueOrderRequest();
        reviewRequest.setDecision("APPROVED");

        rescueOrderService.reviewOrder(
                pendingOrder.getId(),
                admin.getId(),
                reviewRequest
        );
        rescueOrderService.acceptOrder(
                pendingOrder.getId(),
                volunteer.getId()
        );

        mockMvc.perform(
                        get("/api/rescue-orders/public")
                                .param("page", "1")
                                .param("size", "100")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.data.records[?(@.id == %s)].status"
                                        .formatted(
                                                pendingOrder.getId()
                                        )
                        ).value(hasItem("ACCEPTED"))
                );
    }

    @Test
    void rejectsInvalidPublicPage()
            throws Exception {
        mockMvc.perform(
                        get("/api/rescue-orders/public")
                                .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40005)
                );
    }

    private RescueOrderResponse createPendingOrder(
            UserResponse reporter
    ) {
        CreateRescueOrderRequest request =
                new CreateRescueOrderRequest();

        request.setIssueType("INJURED_ANIMAL");
        request.setTitle("公开救助大厅测试工单");
        request.setDescription("发现需要救助的海洋动物");
        request.setLocationText("青岛市测试海滩");
        request.setImageUrls(List.of(
                "https://example.com/rescue-1.jpg",
                "https://example.com/rescue-2.jpg"
        ));

        return rescueOrderService.createOrder(
                reporter.getId(),
                request
        );
    }

    private UserResponse registerAdminUser() {
        return registerRoleUser("ADMIN");
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
                "public_rescue_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("公开工单测试用户");

        return userService.register(request);
    }
}
