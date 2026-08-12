package com.seafish.detection;

public class DetectionClientException extends RuntimeException {

    private final String errorCode;

    public DetectionClientException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
