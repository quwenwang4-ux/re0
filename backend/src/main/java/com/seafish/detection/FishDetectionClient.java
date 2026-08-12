package com.seafish.detection;

import java.math.BigDecimal;
import java.nio.file.Path;

public interface FishDetectionClient {

    AiDetectionOutput detect(
            Path imagePath,
            String originalFileName,
            String modelName,
            BigDecimal confidenceThreshold
    );
}
