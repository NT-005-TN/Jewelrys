package com.jewelry.workshop.security.jwt;

import com.jewelry.workshop.security.auth.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    private final SecretKey signingKey;
    private final String issuer;
    private final String audience;

    @Getter
    private final long jwtExpiration;

    @Getter
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration:900000}") long jwtExpiration,
            @Value("${app.jwt.refresh.expiration:604800000}") long refreshExpiration,
            @Value("${app.jwt.issuer:jewelry-workshop-api}") String issuer,
            @Value("${app.jwt.audience:jewelry-workshop-clients}") String audience
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(Authentication authentication) {
        return generate(authentication, ACCESS_TOKEN, jwtExpiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generate(authentication, REFRESH_TOKEN, refreshExpiration);
    }

    private String generate(Authentication authentication, String tokenType, long lifetimeMillis) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(userPrincipal.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(lifetimeMillis)))
                .id(UUID.randomUUID().toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .signWith(signingKey)
                .compact();
    }

    public String getEmailFromJwt(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateAccessToken(String token) {
        return validateType(token, ACCESS_TOKEN);
    }

    public boolean validateRefreshToken(String token) {
        return validateType(token, REFRESH_TOKEN);
    }

    private boolean validateType(String token, String expectedType) {
        try {
            return expectedType.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
