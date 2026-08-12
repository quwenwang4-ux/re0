package com.seafish.detection;

import java.util.List;

public record AiDetectionOutput(
        String resultImageStorageKey,
        List<AiDetectionTarget> targets
) {
}
