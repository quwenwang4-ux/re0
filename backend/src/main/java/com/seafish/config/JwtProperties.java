package com.seafish.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT 密钥不能为空")
    @Size(
            min = 32,
            message = "JWT 密钥长度不能少于 32 个字符"
    )
    private String secret;

    @Min(
            value = 60,
            message = "JWT 有效时间不能少于 60 秒"
    )
    private long expirationSeconds;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(
            long expirationSeconds
    ) {
        this.expirationSeconds =
                expirationSeconds;
    }
}