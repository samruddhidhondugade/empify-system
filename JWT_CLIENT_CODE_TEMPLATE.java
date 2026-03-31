/**
 * READY-TO-USE CODE TEMPLATE FOR JWT VALIDATION IN ANOTHER SERVICE
 * Copy these classes into your other Spring Boot service
 */

// ============================================================================
// 1. DTO: PublicKeyResponse.java
// ============================================================================
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

// ============================================================================
// 2. SERVICE: PublicKeyLoaderService.java
// ============================================================================
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
    private final RestTemplate restTemplate = new RestTemplate();
    
    @PostConstruct
    public void loadPublicKey() {
        try {
            logger.info("Loading public key from: {}{}", authServiceUrl, publicKeyEndpoint);
            String url = authServiceUrl + publicKeyEndpoint;
            PublicKeyResponse response = restTemplate.getForObject(url, PublicKeyResponse.class);
            
            if (response != null && response.getPublicKey() != null) {
                this.publicKey = parsePublicKey(response.getPublicKey());
                logger.info("✓ Public key loaded successfully. Algorithm: {}", response.getAlgorithm());
            } else {
                throw new RuntimeException("Failed to load public key: Response was empty");
            }
        } catch (Exception e) {
            logger.error("✗ Failed to load public key from auth service", e);
            throw new RuntimeException("Unable to initialize JWT validation: " + e.getMessage(), e);
        }
    }
    
    private PublicKey parsePublicKey(String pemKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String keyContent = pemKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
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
    
    public void reloadPublicKey() {
        logger.info("Reloading public key...");
        loadPublicKey();
    }
}

// ============================================================================
// 3. SERVICE: JwtValidationService.java
// ============================================================================
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

// ============================================================================
// 4. FILTER: JwtAuthenticationFilter.java
// ============================================================================
package com.yourservice.filter;

import com.yourservice.service.JwtValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            String username = jwtValidationService.getUsernameFromToken(token);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        if (path == null) return false;
        return path.startsWith("/actuator/") ||
               path.startsWith("/public/") ||
               path.equals("/health") ||
               path.startsWith("/swagger") ||
               path.startsWith("/api-docs");
    }
}

// ============================================================================
// 5. CONFIG: SecurityConfig.java
// ============================================================================
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
                .sessionManagement(session -> session.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/public/**", "/health", "/swagger/**", "/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

// ============================================================================
// 6. APPLICATION.PROPERTIES
// ============================================================================
// Add these to your application.properties:
//
// auth.service.url=http://localhost:9090
// auth.service.public-key-endpoint=/api/auth/public-key

// ============================================================================
// 7. POM.XML DEPENDENCIES
// ============================================================================
// Add these dependencies:
//
// <dependency>
//     <groupId>io.jsonwebtoken</groupId>
//     <artifactId>jjwt-api</artifactId>
//     <version>0.11.5</version>
// </dependency>
// <dependency>
//     <groupId>io.jsonwebtoken</groupId>
//     <artifactId>jjwt-impl</artifactId>
//     <version>0.11.5</version>
//     <scope>runtime</scope>
// </dependency>
// <dependency>
//     <groupId>io.jsonwebtoken</groupId>
//     <artifactId>jjwt-jackson</artifactId>
//     <version>0.11.5</version>
//     <scope>runtime</scope>
// </dependency>

// ============================================================================
// 8. USAGE EXAMPLE: Controller
// ============================================================================
package com.yourservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {
    
    @GetMapping("/data")
    public ResponseEntity<?> getProtectedData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return ResponseEntity.ok("Protected data for: " + username);
    }
}

