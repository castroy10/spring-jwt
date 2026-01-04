package ru.castroy10.springjwt.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String TEST_USERNAME = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "U2VjcmV0S2V5Rm9ySnNvbldlYlRva2VuU2lnbmluZ0tleQo=");
        ReflectionTestUtils.setField(jwtUtil, "ACCESS_TOKEN_EXPIRATION_MINUTES", 60);
        ReflectionTestUtils.setField(jwtUtil, "REFRESH_TOKEN_EXPIRATION_DAYS", 7);
    }

    @Test
    @DisplayName("Generate Access Token: should generate valid signed token")
    void testGenerateAccessToken() {
        final String token = jwtUtil.generateAccessToken(TEST_USERNAME);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Generate Refresh Token: should generate valid signed token with JTI")
    void testGenerateRefreshToken() {
        final String token = jwtUtil.generateRefreshToken(TEST_USERNAME);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertNotNull(jwtUtil.getJtiFromToken(token));
    }

    @Test
    @DisplayName("Get Username: should extract correct username from token")
    void testGetUsernameFromToken() {
        final String token = jwtUtil.generateAccessToken(TEST_USERNAME);
        final String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    @DisplayName("Is Access Token: should correctly identify access token")
    void testIsAccessToken() {
        final String accessToken = jwtUtil.generateAccessToken(TEST_USERNAME);
        final String refreshToken = jwtUtil.generateRefreshToken(TEST_USERNAME);

        assertTrue(jwtUtil.isAccessToken(accessToken));
        assertFalse(jwtUtil.isAccessToken(refreshToken));
    }

    @Test
    @DisplayName("Is Refresh Token: should correctly identify refresh token")
    void testIsRefreshToken() {
        final String accessToken = jwtUtil.generateAccessToken(TEST_USERNAME);
        final String refreshToken = jwtUtil.generateRefreshToken(TEST_USERNAME);

        assertTrue(jwtUtil.isRefreshToken(refreshToken));
        assertFalse(jwtUtil.isRefreshToken(accessToken));
    }

    @Test
    @DisplayName("Validate Token: should return false for invalid signature or malformed token")
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.string"));
        assertFalse(jwtUtil.validateToken(null));
        
        final String validToken = jwtUtil.generateAccessToken(TEST_USERNAME);
        final String tamperedToken = validToken.substring(0, validToken.length() - 1) + "A";
        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Validate Token: should return false for expired token")
    void testValidateToken_Expired() {
        ReflectionTestUtils.setField(jwtUtil, "ACCESS_TOKEN_EXPIRATION_MINUTES", -1);
        
        final String expiredToken = jwtUtil.generateAccessToken(TEST_USERNAME);
        assertFalse(jwtUtil.validateToken(expiredToken));
    }
}