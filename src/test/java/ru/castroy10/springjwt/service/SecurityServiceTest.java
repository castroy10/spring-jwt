package ru.castroy10.springjwt.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.castroy10.springjwt.model.dto.LoginRequestDto;
import ru.castroy10.springjwt.model.dto.TokenResponseDto;
import ru.castroy10.springjwt.security.JwtUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisService redisService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private SecurityService securityService;

    private final String username = "testUser";
    private final String password = "password";
    private final String refreshToken = "refreshToken";

    @Test
    @DisplayName("Login: Success - should authenticate and return tokens")
    void login_Success() {
        final LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        final String accessToken = "accessToken";
        when(jwtUtil.generateAccessToken(username)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(username)).thenReturn(refreshToken);

        final TokenResponseDto response = securityService.login(loginRequest);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        verify(redisService).saveRefreshToken(username, refreshToken);
    }

    @Test
    @DisplayName("Login: Failure - should throw BadCredentialsException")
    void login_Failure_BadCredentials() {
        final LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> securityService.login(loginRequest));
    }

    @Test
    @DisplayName("Refresh: Success - should rotate tokens")
    void refresh_Success() {
        final String authHeader = "Bearer " + refreshToken;

        when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn(username);
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(redisService.getRefreshToken(username, refreshToken)).thenReturn(refreshToken);

        when(jwtUtil.generateAccessToken(username)).thenReturn("newAccess");
        when(jwtUtil.generateRefreshToken(username)).thenReturn("newRefresh");

        final TokenResponseDto response = securityService.refresh(authHeader);

        assertEquals("newAccess", response.getAccessToken());
        assertEquals("newRefresh", response.getRefreshToken());

        // Verify old token is deleted and new one is saved
        verify(redisService).deleteRefreshToken(username, refreshToken);
        verify(redisService).saveRefreshToken(username, "newRefresh");
    }

    @Test
    @DisplayName("Refresh: Failure - Invalid Header Format")
    void refresh_Failure_InvalidHeader() {
        assertThrows(IllegalArgumentException.class, () -> securityService.refresh("InvalidHeader"));
        assertThrows(IllegalArgumentException.class, () -> securityService.refresh(null));
    }

    @Test
    @DisplayName("Refresh: Failure - Invalid Token Signature")
    void refresh_Failure_InvalidSignature() {
        final String authHeader = "Bearer " + refreshToken;
        when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn(username);
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> securityService.refresh(authHeader));
        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    @DisplayName("Refresh: Failure - Not a Refresh Token (e.g. Access Token)")
    void refresh_Failure_WrongTokenType() {
        final String authHeader = "Bearer " + refreshToken;
        when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn(username);
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(false);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> securityService.refresh(authHeader));
        assertEquals("Provided token is not a refresh token", ex.getMessage());
    }

    @Test
    @DisplayName("Refresh: Failure - Token Not Found in Redis or Mismatch")
    void refresh_Failure_RedisMismatch() {
        final String authHeader = "Bearer " + refreshToken;
        when(jwtUtil.getUsernameFromToken(refreshToken)).thenReturn(username);
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);

        when(redisService.getRefreshToken(username, refreshToken)).thenReturn(null);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> securityService.refresh(authHeader));
        assertEquals("Refresh token is invalid or expired", ex.getMessage());
    }

}
