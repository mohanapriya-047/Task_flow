package com.taskflow.security;

import com.taskflow.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Issues and validates JWTs.
 *
 * Two token "purposes" are supported via a claim:
 *  - PRE_AUTH: short-lived token issued right after password check, before OTP
 *              is verified. Only usable to call the OTP verification endpoints.
 *  - ACCESS:   full session token issued after OTP verification succeeds.
 *
 * This is what prevents a client from skipping the OTP step: endpoints that
 * require full authentication reject PRE_AUTH tokens (see JwtAuthFilter).
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long preAuthExpirationMs;
    private final long accessExpirationMs;

    public JwtUtil(
            @Value("${taskflow.jwt.secret}") String secret,
            @Value("${taskflow.jwt.pre-auth-expiration-ms}") long preAuthExpirationMs,
            @Value("${taskflow.jwt.access-token-expiration-ms}") long accessExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.preAuthExpirationMs = preAuthExpirationMs;
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generatePreAuthToken(User user) {
        return buildToken(user, "PRE_AUTH", preAuthExpirationMs);
    }

    public String generateAccessToken(User user) {
        return buildToken(user, "ACCESS", accessExpirationMs);
    }

    private String buildToken(User user, String purpose, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(user.getUsername())
                .claims(Map.of(
                        "uid", user.getId(),
                        "role", user.getRole().name(),
                        "purpose", purpose
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Generic short-lived signed token for multi-step flows that aren't tied to
     * a single "logged in as" user session (e.g. the Manager -> Admin
     * dual-OTP authorization handshake). Never accepted by JwtAuthFilter -
     * only read explicitly by the service that issued it.
     */
    public String generateGenericToken(Map<String, Object> claims, String purpose, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        Map<String, Object> allClaims = new java.util.HashMap<>(claims);
        allClaims.put("purpose", purpose);
        return Jwts.builder()
                .claims(allClaims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return "ACCESS".equals(claims.get("purpose", String.class));
    }
}
