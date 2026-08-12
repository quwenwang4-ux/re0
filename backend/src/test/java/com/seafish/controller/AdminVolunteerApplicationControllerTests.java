package com.seafish.controller;

import com.seafish.controller.request.CreateVolunteerApplicationRequest;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.controller.response.VolunteerApplicationResponse;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.service.UserService;
import com.seafish.service.VolunteerApplicationService;
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
class AdminVolunteerApplicationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private VolunteerApplicationService
            volunteerApplicationService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Test
    void requiresAuthenticationToListApplications()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/volunteer-applications")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNormalUserWhenListingApplications()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/volunteer-applications")
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
    void allowsAdminToListPendingApplications()
            throws Exception {
        UserResponse applicant = registerTestUser();

        CreateVolunteerApplicationRequest request =
                new CreateVolunteerApplicationRequest();

        request.setRealName("管理员分页测试申请人");
        request.setPhone("13800138001");
        request.setRegion("山东青岛");
        request.setSkills("海洋动物救助");
        request.setReason("参加志愿服务");

        volunteerApplicationService.createApplication(
                applicant.getId(),
                request
        );

        mockMvc.perform(
                        adminGet()
                                .param("status", "PENDING")
                                .param("page", "1")
                                .param("size", "100")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.page")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.size")
                                .value(100)
                )
                .andExpect(
                        jsonPath(
                                "$.data.records[*].applicantId"
                        ).value(
                                hasItem(
                                        applicant
                                                .getId()
                                                .intValue()
                                )
                        )
                );
    }

    @Test
    void rejectsInvalidStatusForAdmin()
            throws Exception {
        mockMvc.perform(
                        adminGet()
                                .param("status", "UNKNOWN")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40007)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("申请状态不合法")
                );
    }

    @Test
    @Transactional
    void approvesApplicationAndAssignsVolunteerRole()
            throws Exception {
        UserResponse applicant = registerTestUser();
        UserResponse admin = registerAdminUser();

        VolunteerApplicationResponse application =
                createPendingApplication(applicant);

        mockMvc.perform(
                        adminReview(
                                application.getId(),
                                admin
                        ).content("""
                                {
                                  "status": "APPROVED",
                                  "reviewComment": "申请资料符合要求"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.status")
                                .value("APPROVED")
                )
                .andExpect(
                        jsonPath("$.data.reviewerId")
                                .value(admin.getId())
                );

        assertTrue(
                sysUserRoleMapper
                        .selectActiveRoleCodes(
                                applicant.getId()
                        )
                        .contains("VOLUNTEER")
        );
    }

    @Test
    @Transactional
    void rejectsApplicationWithReviewComment()
            throws Exception {
        UserResponse applicant = registerTestUser();
        UserResponse admin = registerAdminUser();

        VolunteerApplicationResponse application =
                createPendingApplication(applicant);

        mockMvc.perform(
                        adminReview(
                                application.getId(),
                                admin
                        ).content("""
                                {
                                  "status": "REJECTED",
                                  "reviewComment": "申请资料不完整"
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
                                .value("申请资料不完整")
                );
    }

    @Test
    @Transactional
    void requiresCommentWhenRejectingApplication()
            throws Exception {
        UserResponse applicant = registerTestUser();
        UserResponse admin = registerAdminUser();

        VolunteerApplicationResponse application =
                createPendingApplication(applicant);

        mockMvc.perform(
                        adminReview(
                                application.getId(),
                                admin
                        ).content("""
                                {
                                  "status": "REJECTED"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40008)
                );
    }

    @Test
    @Transactional
    void rejectsRepeatedReview()
            throws Exception {
        UserResponse applicant = registerTestUser();
        UserResponse admin = registerAdminUser();

        VolunteerApplicationResponse application =
                createPendingApplication(applicant);

        mockMvc.perform(
                        adminReview(
                                application.getId(),
                                admin
                        ).content("""
                                {
                                  "status": "APPROVED"
                                }
                                """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        adminReview(
                                application.getId(),
                                admin
                        ).content("""
                                {
                                  "status": "REJECTED",
                                  "reviewComment": "再次审批"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value(40907)
                );
    }

    private MockHttpServletRequestBuilder adminGet() {
        return get("/api/admin/volunteer-applications")
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
            Long applicationId,
            UserResponse admin
    ) {
        return put(
                "/api/admin/volunteer-applications/{id}/review",
                applicationId
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

    private VolunteerApplicationResponse
            createPendingApplication(
                    UserResponse applicant
            ) {
        CreateVolunteerApplicationRequest request =
                new CreateVolunteerApplicationRequest();

        request.setRealName("审批测试申请人");
        request.setPhone("13800138003");
        request.setRegion("山东青岛");
        request.setSkills("海洋动物救助");
        request.setReason("希望参加志愿服务");

        return volunteerApplicationService
                .createApplication(
                        applicant.getId(),
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
                "admin_list_" + UUID
                        .randomUUID()
                        .toString()
                        .replace("-", "");

        RegisterRequest request =
                new RegisterRequest();

        request.setUsername(username);
        request.setPassword("Ocean1234");
        request.setNickname("管理员列表测试用户");

        return userService.register(request);
    }
}
