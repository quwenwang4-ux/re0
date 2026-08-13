package com.seafish.detection;

import com.seafish.service.FileStorageService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class YoloServiceDetectionClientTests {
    private final MockWebServer server = new MockWebServer();

    @AfterEach
    void closeServer() throws Exception { server.close(); }

    @Test
    void convertsYoloResponseAndStoresRenderedImage() throws Exception {
        Path storage = Files.createTempDirectory("seafish-yolo-test");
        Path image = Files.createTempFile("input", ".jpg");
        String rendered = Base64.getEncoder().encodeToString(new byte[]{10,20,30});
        server.enqueue(new MockResponse().setHeader("Content-Type","application/json").setBody("""
                {"modelName":"best","durationMs":120,"targets":[{"className":"Sharks","confidence":0.91,"boxX1":10,"boxY1":20,"boxX2":200,"boxY2":180}],"resultImageBase64":"%s"}
                """.formatted(rendered)));
        server.start();
        YoloServiceDetectionClient client = new YoloServiceDetectionClient(
                WebClient.builder(), new FileStorageService(storage.toString()),
                server.url("/").toString(), 5);

        AiDetectionOutput output = client.detect(image,"shark.jpg","best",new BigDecimal("0.5"));

        assertEquals(1,output.targets().size());
        assertEquals("Sharks",output.targets().get(0).className());
        assertNotNull(output.resultImageStorageKey());
        assertTrue(Files.exists(storage.resolve(output.resultImageStorageKey())));
    }

    @Test
    void convertsServiceFailureToBusinessFriendlyError() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(503)); server.start();
        Path image = Files.createTempFile("input", ".jpg");
        YoloServiceDetectionClient client = new YoloServiceDetectionClient(
                WebClient.builder(), new FileStorageService(Files.createTempDirectory("storage").toString()),
                server.url("/").toString(), 5);
        DetectionClientException error = assertThrows(DetectionClientException.class,
                () -> client.detect(image,"fish.jpg","best",new BigDecimal("0.5")));
        assertEquals("AI_BAD_RESPONSE",error.getErrorCode());
    }
}
