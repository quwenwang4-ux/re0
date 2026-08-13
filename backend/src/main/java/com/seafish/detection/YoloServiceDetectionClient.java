package com.seafish.detection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.seafish.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "seafish.detection.provider",
        havingValue = "yolo-service"
)
public class YoloServiceDetectionClient implements FishDetectionClient {

    private final WebClient webClient;
    private final FileStorageService fileStorageService;
    private final Duration timeout;

    public YoloServiceDetectionClient(
            WebClient.Builder builder,
            FileStorageService fileStorageService,
            @Value("${seafish.detection.service-url:http://localhost:8000}")
            String serviceUrl,
            @Value("${seafish.detection.timeout-seconds:60}")
            long timeoutSeconds
    ) {
        this.webClient = builder
                .baseUrl(serviceUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(20 * 1024 * 1024))
                .build();
        this.fileStorageService = fileStorageService;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    @Override
    public AiDetectionOutput detect(
            Path imagePath,
            String originalFileName,
            String modelName,
            BigDecimal confidenceThreshold
    ) {
        MultiValueMap<String, Object> form =
                new LinkedMultiValueMap<>();
        form.add("image", new FileSystemResource(imagePath));
        form.add("confidence_threshold", confidenceThreshold.toPlainString());

        try {
            YoloResponse response = webClient.post()
                    .uri("/api/v1/detect")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(form))
                    .retrieve()
                    .bodyToMono(YoloResponse.class)
                    .block(timeout);

            if (response == null) {
                throw new DetectionClientException(
                        "AI_EMPTY_RESPONSE",
                        "AI识别服务没有返回结果"
                );
            }

            String resultKey = null;
            if (response.resultImageBase64() != null
                    && !response.resultImageBase64().isBlank()) {
                byte[] rendered = Base64.getDecoder().decode(
                        response.resultImageBase64()
                );
                resultKey = fileStorageService
                        .saveDetectionResultImage(rendered);
            }

            List<AiDetectionTarget> targets = response.targets() == null
                    ? List.of()
                    : response.targets().stream()
                    .map(target -> new AiDetectionTarget(
                            target.className(),
                            target.confidence(),
                            target.boxX1(),
                            target.boxY1(),
                            target.boxX2(),
                            target.boxY2()
                    ))
                    .toList();
            return new AiDetectionOutput(resultKey, targets);
        } catch (WebClientResponseException exception) {
            throw new DetectionClientException(
                    "AI_BAD_RESPONSE",
                    "AI识别服务返回错误：" + exception.getStatusCode().value()
            );
        } catch (DetectionClientException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new DetectionClientException(
                    "AI_SERVICE_UNAVAILABLE",
                    "无法连接AI识别服务，请确认Python服务已启动"
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record YoloResponse(
            String modelName,
            Long durationMs,
            List<YoloTarget> targets,
            String resultImageBase64
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record YoloTarget(
            String className,
            BigDecimal confidence,
            BigDecimal boxX1,
            BigDecimal boxY1,
            BigDecimal boxX2,
            BigDecimal boxY2
    ) {
    }
}
