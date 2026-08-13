package com.seafish.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "seafish.storage.root=target/test-uploads")
@AutoConfigureMockMvc
class FileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requiresLoginToUploadRescueImage() throws Exception {
        mockMvc.perform(multipart("/api/files/rescue-images")
                        .file(image()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loggedInUserCanUploadAndGuestCanViewImage() throws Exception {
        String response = mockMvc.perform(
                        multipart("/api/files/rescue-images")
                                .file(image())
                                .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String url = response.replaceAll(
                ".*\\\"url\\\":\\\"([^\\\"]+)\\\".*",
                "$1"
        );
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    private MockMultipartFile image() {
        return new MockMultipartFile(
                "image",
                "rescue.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );
    }
}
