package com.jewelry.workshop.security.jwt;

import com.jewelry.workshop.security.auth.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;
import java.util.Date;


@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Getter
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Getter
    @Value("${app.jwt.refresh.expiration}")
    private long refreshExpiration;

    private javax.crypto.SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("version", userPrincipal.getUser().getTokenVersion())
                .claim("token_type", "access")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpiration))
                .signWith(key(), Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication){
       UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("version", userPrincipal.getUser().getTokenVersion())
                .claim("token_type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + refreshExpiration))
                .signWith(key(), Jwts.SIG.HS512)
                .compact();
    }

    public String getSubject(String token){
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean matchesVersion(String token, long version) {
        Number value = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token)
                .getPayload().get("version", Number.class);
        return value != null && value.longValue() == version;
    }

    public boolean validateToken(String token) {
        return validateTokenType(token, "access");
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, "refresh");
    }

    private boolean validateTokenType(String token, String expectedType){
        try{
            Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
            return expectedType.equals(claims.get("token_type", String.class));
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

}
