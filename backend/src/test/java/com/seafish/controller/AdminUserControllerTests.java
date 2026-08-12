package com.seafish.controller;

import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.mapper.SysUserRoleMapper;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private SysUserRoleMapper roleMapper;

    @Test
    void rejectsNormalUser() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().jwt(token -> token.subject("1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void listsUsersWithoutPasswordHash() throws Exception {
        UserResponse admin = registerAdmin();
        UserResponse target = registerUser();

        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", target.getUsername())
                        .with(adminJwt(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].username")
                        .value(target.getUsername()))
                .andExpect(jsonPath("$.data.records[0].passwordHash")
                        .doesNotExist());
    }

    @Test
    @Transactional
    void disablesAnotherUser() throws Exception {
        UserResponse admin = registerAdmin();
        UserResponse target = registerUser();

        mockMvc.perform(put("/api/admin/users/{id}/status", target.getId())
                        .with(adminJwt(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DISABLED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    @Test
    @Transactional
    void preventsAdminFromDisablingSelf() throws Exception {
        UserResponse admin = registerAdmin();

        mockMvc.perform(put("/api/admin/users/{id}/status", admin.getId())
                        .with(adminJwt(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DISABLED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40041));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor
    adminJwt(UserResponse admin) {
        return jwt().jwt(token -> token.subject(admin.getId().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private UserResponse registerAdmin() {
        UserResponse admin = registerUser();
        assertTrue(roleMapper.assignOrReactivateRole(
                admin.getId(), "ADMIN", null
        ) >= 1);
        return admin;
    }

    private UserResponse registerUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin_user_test_" + UUID.randomUUID()
                .toString().replace("-", ""));
        request.setPassword("Ocean1234");
        return userService.register(request);
    }
}
