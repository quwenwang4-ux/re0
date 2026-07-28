package com.seafish.controller.response;

public class HealthResponse {
    private final String status;
    private final String application;
    private final String version;

    public HealthResponse(
            String status,
            String application,
            String version
    ) {
        this.status = status;
        this.application = application;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }
}
