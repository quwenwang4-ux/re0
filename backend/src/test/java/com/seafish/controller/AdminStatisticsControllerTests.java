package com.seafish.controller;

import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminStatisticsControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private SysUserRoleMapper roleMapper;

    @Test
    void requiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/overview")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void returnsOverviewForAdmin() throws Exception {
        UserResponse admin = registerUser();
        assertTrue(roleMapper.assignOrReactivateRole(
                admin.getId(), "ADMIN", null
        ) >= 1);

        mockMvc.perform(get("/api/admin/statistics/overview")
                        .with(jwt()
                                .jwt(token -> token.subject(
                                        admin.getId().toString()
                                ))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADMIN")
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeUserCount").isNumber())
                .andExpect(jsonPath("$.data.detectionCount").isNumber())
                .andExpect(jsonPath("$.data.rescueOrdersByStatus").isArray())
                .andExpect(jsonPath("$.data.topDetectedClasses").isArray());
    }

    private UserResponse registerUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("statistics_" + UUID.randomUUID()
                .toString().replace("-", ""));
        request.setPassword("Ocean1234");
        return userService.register(request);
    }
}
