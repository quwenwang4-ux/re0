package com.seafish.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FishInfoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requiresAuthenticationToCreateFish()
            throws Exception {
        mockMvc.perform(
                        post("/api/fishes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiresAdminRoleToCreateFish()
            throws Exception {
        mockMvc.perform(
                        post("/api/fishes")
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_USER"
                                        )
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsBlankCreateRequest() throws Exception {
        String requestJson = """
                {
                  "chineseName": "",
                  "sourceType": ""
                }
                """;

        mockMvc.perform(
                        post("/api/fishes")
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestJson)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(40001)
                )
                .andExpect(
                        jsonPath("$.message")
                                .isNotEmpty()
                );
    }
}
