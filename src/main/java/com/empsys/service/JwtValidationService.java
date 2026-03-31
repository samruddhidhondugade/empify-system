package com.empsys.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtValidationService {
    @Autowired
    private PublicKeyLoaderService publicKeyLoaderService;
    
    private JwtParser jwtParser;
    
    @PostConstruct
    public void init() {
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(publicKeyLoaderService.getPublicKey())
                .build();
    }
    
    public Claims validateToken(String token) throws Exception {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            if (claims.getExpiration().before(new Date())) {
                throw new Exception("Token has expired");
            }
            return claims;
        } catch (Exception e) {
            throw new Exception("Invalid JWT token: " + e.getMessage(), e);
        }
    }
    
    public String getUsernameFromToken(String token) throws Exception {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
    
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

