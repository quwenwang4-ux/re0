package com.seafish.service;

import com.seafish.config.JwtProperties;
import com.seafish.entity.SysUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    private static final String ISSUER =
            "https://seafish.local";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String createAccessToken(
            SysUser user,
            List<String> roles
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(
                jwtProperties.getExpirationSeconds()
        );

        JwtClaimsSet claims = JwtClaimsSet
                .builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        Jwt token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        header,
                        claims
                )
        );

        return token.getTokenValue();
    }

    public long getExpirationSeconds() {
        return jwtProperties.getExpirationSeconds();
    }
}
