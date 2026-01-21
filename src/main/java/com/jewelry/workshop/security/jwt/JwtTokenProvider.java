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
import java.time.Duration;
import java.util.Date;


@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Getter
    @Value("${app.jwt.refresh.expiration}")
    private long refreshExpiration;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpiration))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication){
       UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + refreshExpiration))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromJwt(String token){
        return Jwts.parser().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

}
