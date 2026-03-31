# JWT Client Integration Guide

## Overview
This guide shows how to integrate JWT token validation in another Spring Boot service using the public key from the auth service.

## Architecture
1. **On Startup**: Fetch public key from auth service API and store in memory
2. **On Each Request**: Validate JWT token using the stored public key
3. **Filter**: Intercept requests and validate tokens automatically

---

## Step 1: Add Dependencies

Add to your `pom.xml`:

```xml
<dependencies>
    <!-- JWT Dependencies -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Spring Web Client for fetching public key -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

---

## Step 2: Configuration Properties

Add to `application.properties`:

```properties
# Auth Service Configuration
auth.service.url=http://localhost:9090
auth.service.public-key-endpoint=/api/auth/public-key

# JWT Configuration (optional)
jwt.public-key-refresh-interval=3600000
```

---

## Step 3: Public Key Response DTO

Create `PublicKeyResponse.java`:

```java
package com.yourservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyResponse {
    private String publicKey;
    private String algorithm;
    private String format;
    private String keyType;
}
```

---

## Step 4: Public Key Loader Service

Create `PublicKeyLoaderService.java`:

```java
package com.yourservice.service;

import com.yourservice.dto.PublicKeyResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class PublicKeyLoaderService {
    
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyLoaderService.class);
    
    @Value("${auth.service.url:http://localhost:9090}")
    private String authServiceUrl;
    
    @Value("${auth.service.public-key-endpoint:/api/auth/public-key}")
    private String publicKeyEndpoint;
    
    private PublicKey publicKey;
    private RestTemplate restTemplate;
    
    public PublicKeyLoaderService() {
        this.restTemplate = new RestTemplate();
    }
    
    @PostConstruct
    public void loadPublicKey() {
        try {
            logger.info("Loading public key from auth service: {}{}", authServiceUrl, publicKeyEndpoint);
            
            String url = authServiceUrl + publicKeyEndpoint;
            PublicKeyResponse response = restTemplate.getForObject(url, PublicKeyResponse.class);
            
            if (response != null && response.getPublicKey() != null) {
                this.publicKey = parsePublicKey(response.getPublicKey());
                logger.info("Public key loaded successfully. Algorithm: {}", response.getAlgorithm());
            } else {
                throw new RuntimeException("Failed to load public key: Response was empty");
            }
        } catch (Exception e) {
            logger.error("Failed to load public key from auth service", e);
            throw new RuntimeException("Unable to initialize JWT validation: " + e.getMessage(), e);
        }
    }
    
    private PublicKey parsePublicKey(String pemKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Remove PEM headers and whitespace
        String keyContent = pemKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        // Decode Base64
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        
        // Create PublicKey object
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
    
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            throw new IllegalStateException("Public key not loaded. Service initialization failed.");
        }
        return publicKey;
    }
    
    /**
     * Reload public key from auth service (useful for key rotation)
     */
    public void reloadPublicKey() {
        logger.info("Reloading public key from auth service");
        loadPublicKey();
    }
}
```

---

## Step 5: JWT Validation Service

Create `JwtValidationService.java`:

```java
package com.yourservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtValidationService {
    
    @Autowired
    private PublicKeyLoaderService publicKeyLoaderService;
    
    private JwtParser jwtParser;
    
    @Autowired
    public void init() {
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(publicKeyLoaderService.getPublicKey())
                .build();
    }
    
    /**
     * Validate and parse JWT token
     * @param token JWT token string
     * @return Claims object if valid
     * @throws Exception if token is invalid
     */
    public Claims validateToken(String token) throws Exception {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            
            // Additional validation: check expiration
            if (claims.getExpiration().before(new Date())) {
                throw new Exception("Token has expired");
            }
            
            return claims;
        } catch (Exception e) {
            throw new Exception("Invalid JWT token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) throws Exception {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
    
    /**
     * Check if token is valid (without throwing exception)
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## Step 6: JWT Authentication Filter

Create `JwtAuthenticationFilter.java`:

```java
package com.yourservice.filter;

import com.yourservice.service.JwtValidationService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtValidationService jwtValidationService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            String username = jwtValidationService.getUsernameFromToken(token);
            
            // Set authentication in Spring Security context
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("JWT authentication successful for user: {}", username);
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        if (path == null) return false;
        // Add your public endpoints here
        return path.startsWith("/actuator/") ||
               path.startsWith("/public/") ||
               path.equals("/health");
    }
}
```

---

## Step 7: Security Configuration

Create `SecurityConfig.java`:

```java
package com.yourservice.config;

import com.yourservice.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/public/**", "/health").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

---

## Step 8: Usage in Controllers

```java
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    @GetMapping("/protected")
    public ResponseEntity<?> getProtectedData() {
        // User is automatically authenticated by filter
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return ResponseEntity.ok("Data for user: " + username);
    }
}
```

---

## Step 9: Testing

### Test with Postman:

1. **Get Token** (from auth service):
   ```
   POST http://localhost:9090/api/auth/login
   Body: {"username": "admin", "password": "12345"}
   ```

2. **Use Token** (in your service):
   ```
   GET http://localhost:YOUR_PORT/api/data/protected
   Header: Authorization: Bearer <token>
   ```

---

## Optional: Scheduled Public Key Refresh

If you want to refresh the public key periodically:

```java
@Scheduled(fixedDelayString = "${jwt.public-key-refresh-interval:3600000}")
public void refreshPublicKey() {
    publicKeyLoaderService.reloadPublicKey();
}
```

Add `@EnableScheduling` to your main application class.

---

## Summary

✅ Public key loads on startup from auth service
✅ Stored in memory for fast access
✅ JWT tokens validated automatically on each request
✅ Spring Security integration for endpoint protection
✅ Easy to configure and extend

