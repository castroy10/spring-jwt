package ru.castroy10.springjwt.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.accessTokenExpirationMinutes}")
    private int ACCESS_TOKEN_EXPIRATION_MINUTES;
    @Value("${jwt.refreshTokenExpirationDays}")
    private int REFRESH_TOKEN_EXPIRATION_DAYS;
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secretKey}")
    private String secretKey;

    /**
     * Generates a new access token for the specified user.
     *
     * @param userName the username for whom the token is generated
     * @return the generated access token string
     */
    public String generateAccessToken(final String userName) {
        final Instant now = Instant.now();
        final Instant expiry = now.plus(ACCESS_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        return Jwts.builder()
                   .subject(userName)
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(expiry))
                   .claim(CLAIM_TYPE, TYPE_ACCESS)
                   .signWith(getSignInKey())
                   .compact();
    }

    /**
     * Generates a new refresh token for the specified user.
     * The token includes a unique identifier (JTI).
     *
     * @param userName the username for whom the token is generated
     * @return the generated refresh token string
     */
    public String generateRefreshToken(final String userName) {
        final Instant now = Instant.now();
        final Instant expiry = now.plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS);
        final String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                   .subject(userName)
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(expiry))
                   .id(jti)
                   .claim(CLAIM_TYPE, TYPE_REFRESH)
                   .signWith(getSignInKey())
                   .compact();
    }

    /**
     * Extracts the JTI (JWT ID) from the given refresh token.
     *
     * @param refreshToken the refresh token
     * @return the JTI string extracted from the token
     */
    public String getJtiFromToken(final String refreshToken){
        return Jwts.parser()
                   .verifyWith(getSignInKey())
                   .build()
                   .parseSignedClaims(refreshToken)
                   .getPayload()
                   .getId();
    }

    /**
     * Validates the given token.
     * Checks signature, expiration, and malformed structure.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(final String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (final JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the given token.
     *
     * @param token the token
     * @return the username extracted from the token
     */
    public String getUsernameFromToken(final String token) {
        return Jwts.parser()
                   .verifyWith(getSignInKey())
                   .build()
                   .parseSignedClaims(token)
                   .getPayload()
                   .getSubject();
    }

    /**
     * Checks if the provided token is an access token.
     *
     * @param token the token to check
     * @return true if the token type claim is "access", false otherwise
     */
    public boolean isAccessToken(final String token) {
        try {
            final String type = Jwts.parser()
                                    .verifyWith(getSignInKey())
                                    .build()
                                    .parseSignedClaims(token)
                                    .getPayload()
                                    .get(CLAIM_TYPE, String.class);
            return TYPE_ACCESS.equals(type);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Checks if the provided token is a refresh token.
     *
     * @param token the token to check
     * @return true if the token type claim is "refresh", false otherwise
     */
    public boolean isRefreshToken(final String token) {
        try {
            final String type = Jwts.parser()
                                    .verifyWith(getSignInKey())
                                    .build()
                                    .parseSignedClaims(token)
                                    .getPayload()
                                    .get(CLAIM_TYPE, String.class);
            return TYPE_REFRESH.equals(type);
        } catch (final Exception e) {
            return false;
        }
    }

    private SecretKey getSignInKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
