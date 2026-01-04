package ru.castroy10.springjwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.castroy10.springjwt.model.dto.LoginRequestDto;
import ru.castroy10.springjwt.model.dto.TokenResponseDto;
import ru.castroy10.springjwt.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private static final String BEARER = "Bearer ";
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticates the user and generates access and refresh tokens.
     *
     * @param loginRequestDto the login request containing username and password
     * @return a DTO containing the generated access and refresh tokens
     * @throws org.springframework.security.authentication.BadCredentialsException if authentication fails
     */
    public TokenResponseDto login(final LoginRequestDto loginRequestDto) {
        final Authentication authentication = authenticate(loginRequestDto);
        final String userName = authentication.getName();
        return generateTokens(userName);
    }

    /**
     * Refreshes the authentication tokens using a valid refresh token.
     * Validates the old refresh token, invalidates it in Redis, and issues a new pair.
     *
     * @param authHeader the authorization header containing the bearer refresh token
     * @return a DTO containing the new access and refresh tokens
     * @throws IllegalArgumentException if the token is invalid, expired, or missing
     */
    public TokenResponseDto refresh(final String authHeader) {
        final String token = extractToken(authHeader);
        final String userName = jwtUtil.getUsernameFromToken(token);
        validateRefreshToken(token, userName);
        return generateTokens(userName);
    }

    private TokenResponseDto generateTokens(final String userName) {
        final String accessToken = jwtUtil.generateAccessToken(userName);
        final String refreshToken = jwtUtil.generateRefreshToken(userName);
        redisService.saveRefreshToken(userName, refreshToken);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    private void validateRefreshToken(final String refreshToken, final String userName) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid token");
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Provided token is not a refresh token");
        }
        final String storedToken = redisService.getRefreshToken(userName, refreshToken);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }
        redisService.deleteRefreshToken(userName, refreshToken);
    }

    private String extractToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        return authHeader.substring(BEARER.length());
    }

    private Authentication authenticate(final LoginRequestDto request) {
        try {
            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (final AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

}
