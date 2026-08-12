package com.seafish.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.entity.FishProfileTask;
import com.seafish.mapper.FishProfileTaskMapper;
import com.seafish.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "seafish.storage.root=target/test-uploads")
@AutoConfigureMockMvc
class DetectionControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private FishProfileTaskMapper taskMapper;

    @Test
    void requiresAuthenticationToDetect() throws Exception {
        mockMvc.perform(multipart("/api/detections")
                        .file(image("clown-fish.jpg")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void detectsKnownFishAndStoresRecord() throws Exception {
        UserResponse user = registerUser();
        mockMvc.perform(multipart("/api/detections")
                        .file(image("clown-fish.jpg"))
                        .param("confidenceThreshold", "0.5")
                        .with(userJwt(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.results[0].className").value("小丑鱼"))
                .andExpect(jsonPath("$.data.originalImageUrl").isNotEmpty());
    }

    @Test
    @Transactional
    void recordsNoTargetReason() throws Exception {
        UserResponse user = registerUser();
        mockMvc.perform(multipart("/api/detections")
                        .file(image("empty-ocean.jpg"))
                        .with(userJwt(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NO_TARGET"))
                .andExpect(jsonPath("$.data.errorCode").value("NO_TARGET"));
    }

    @Test
    @Transactional
    void recordsAiServiceFailure() throws Exception {
        UserResponse user = registerUser();
        mockMvc.perform(multipart("/api/detections")
                        .file(image("ai-error.jpg"))
                        .with(userJwt(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.errorCode").value("AI_SERVICE_ERROR"));
    }

    @Test
    @Transactional
    void createsProfileTaskForUnknownClass() throws Exception {
        UserResponse user = registerUser();
        mockMvc.perform(multipart("/api/detections")
                        .file(image("unknown-fish.jpg"))
                        .with(userJwt(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[0].fishProfileMissing").value(true));

        QueryWrapper<FishProfileTask> query = new QueryWrapper<>();
        query.eq("class_name", "待补充鱼类").eq("status", "PENDING");
        assertEquals(1L, taskMapper.selectCount(query));
    }

    @Test
    @Transactional
    void preventsViewingAnotherUsersRecord() throws Exception {
        UserResponse owner = registerUser();
        UserResponse other = registerUser();
        String response = mockMvc.perform(multipart("/api/detections")
                        .file(image("empty-ocean.jpg"))
                        .with(userJwt(owner)))
                .andReturn().getResponse().getContentAsString();
        long recordId = Long.parseLong(
                response.replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
        );

        mockMvc.perform(get("/api/detections/{id}", recordId)
                        .with(userJwt(other)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40330));
    }

    private MockMultipartFile image(String name) {
        return new MockMultipartFile(
                "image", name, "image/jpeg", new byte[]{1, 2, 3}
        );
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor
    userJwt(UserResponse user) {
        return jwt().jwt(token -> token.subject(user.getId().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private UserResponse registerUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("detection_" + UUID.randomUUID().toString().replace("-", ""));
        request.setPassword("Ocean1234");
        return userService.register(request);
    }
}
