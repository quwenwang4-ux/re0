package com.seafish.detection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(
        name = "seafish.detection.provider",
        havingValue = "demo",
        matchIfMissing = true
)
public class DemoFishDetectionClient
        implements FishDetectionClient {

    @Override
    public AiDetectionOutput detect(
            Path imagePath,
            String originalFileName,
            String modelName,
            BigDecimal confidenceThreshold
    ) {
        String name = originalFileName == null
                ? ""
                : originalFileName.toLowerCase(Locale.ROOT);

        if (name.contains("ai-error")) {
            throw new DetectionClientException(
                    "AI_SERVICE_ERROR",
                    "AI识别服务暂时不可用，请稍后重试"
            );
        }

        if (name.contains("clown")
                || name.contains("小丑")) {
            return output("小丑鱼", "0.9200");
        }

        if (name.contains("blue-tang")
                || name.contains("bluetang")
                || name.contains("蓝吊")) {
            return output("蓝吊", "0.8900");
        }

        if (name.contains("unknown")) {
            return output("待补充鱼类", "0.8700");
        }

        return new AiDetectionOutput(null, List.of());
    }

    private AiDetectionOutput output(
            String className,
            String confidence
    ) {
        AiDetectionTarget target =
                new AiDetectionTarget(
                        className,
                        new BigDecimal(confidence),
                        new BigDecimal("10.0000"),
                        new BigDecimal("20.0000"),
                        new BigDecimal("210.0000"),
                        new BigDecimal("180.0000")
                );

        return new AiDetectionOutput(
                null,
                List.of(target)
        );
    }
}
