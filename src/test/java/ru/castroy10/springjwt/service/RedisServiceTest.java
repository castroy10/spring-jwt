package ru.castroy10.springjwt.service;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import ru.castroy10.springjwt.security.JwtUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    private final String username = "testUser";
    private final String token = "testToken";
    private final String jti = "testJti";
    private final String expectedKey = "refresh_token:" + username + ":" + jti;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisService, "REFRESH_TOKEN_EXPIRATION_DAYS", 7);
    }

    @Test
    @DisplayName("Save Refresh Token: should save token to Redis with TTL")
    void saveRefreshToken() {
        when(jwtUtil.getJtiFromToken(token)).thenReturn(jti);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.saveRefreshToken(username, token);

        verify(valueOperations).set(eq(expectedKey), eq(token), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("Get Refresh Token: should retrieve token from Redis")
    void getRefreshToken() {
        when(jwtUtil.getJtiFromToken(token)).thenReturn(jti);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(token);

        final String result = redisService.getRefreshToken(username, token);

        assertEquals(token, result);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("Delete Refresh Token: should remove token from Redis")
    void deleteRefreshToken() {
        when(jwtUtil.getJtiFromToken(token)).thenReturn(jti);

        redisService.deleteRefreshToken(username, token);

        verify(redisTemplate).delete(expectedKey);
    }
}
