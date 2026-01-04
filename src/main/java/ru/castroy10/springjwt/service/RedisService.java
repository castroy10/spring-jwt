package ru.castroy10.springjwt.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.castroy10.springjwt.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final JwtUtil jwtUtil;

    @Value("${jwt.refreshTokenExpirationDays}")
    private int REFRESH_TOKEN_EXPIRATION_DAYS;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Saves the refresh token to Redis with a specific expiration time.
     * Uses the username and token JTI as the key.
     *
     * @param userName the username associated with the token
     * @param token    the refresh token string
     */
    public void saveRefreshToken(final String userName, final String token) {
        final String jti = jwtUtil.getJtiFromToken(token);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userName + ":" + jti, token, REFRESH_TOKEN_EXPIRATION_DAYS, TimeUnit.DAYS);
    }

    /**
     * Retrieves the refresh token from Redis.
     *
     * @param userName the username associated with the token
     * @param token    the refresh token string (used to extract JTI for key lookup)
     * @return the stored refresh token string, or null if not found
     */
    public String getRefreshToken(final String userName, final String token) {
        final String jti = jwtUtil.getJtiFromToken(token);
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userName + ":" + jti);
    }

    /**
     * Deletes the refresh token from Redis.
     *
     * @param userName the username associated with the token
     * @param token    the refresh token string (used to extract JTI for key lookup)
     */
    public void deleteRefreshToken(final String userName, final String token) {
        final String jti = jwtUtil.getJtiFromToken(token);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userName + ":" + jti);
    }

}
