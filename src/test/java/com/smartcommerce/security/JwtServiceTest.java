package com.smartcommerce.security;

import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKeyString",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
        jwtService.init();

        testUser = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(Role.USER)
                .active(true)
                .build();
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnUserEmail() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("user@test.com");
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_withWrongUser_shouldReturnFalse() {
        String token = jwtService.generateToken(testUser);
        User otherUser = User.builder().email("other@test.com").active(true).role(Role.USER).build();
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void extractClaims_withExpiredToken_shouldThrowJwtException() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken = jwtService.generateToken(testUser);
        assertThatThrownBy(() -> jwtService.extractClaims(expiredToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractClaims_withTamperedToken_shouldThrowJwtException() {
        String token = jwtService.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtService.extractClaims(tamperedToken))
                .isInstanceOf(JwtException.class);
    }
}
