package com.seafish.service;

import com.seafish.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TokenServiceTests {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void createsVerifiableAccessToken() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("token_test_user");

        List<String> roles = List.of(
                "USER",
                "VOLUNTEER"
        );

        String tokenValue =
                tokenService.createAccessToken(
                        user,
                        roles
                );

        assertNotNull(tokenValue);

        Jwt token = jwtDecoder.decode(tokenValue);

        assertEquals("100", token.getSubject());
        assertEquals(
                "token_test_user",
                token.getClaimAsString("username")
        );
        assertEquals(
                roles,
                token.getClaimAsStringList("roles")
        );
        assertEquals(
                "https://seafish.local",
                token.getIssuer().toString()
        );
        assertTrue(
                token.getExpiresAt().isAfter(
                        Instant.now()
                )
        );
    }
}
