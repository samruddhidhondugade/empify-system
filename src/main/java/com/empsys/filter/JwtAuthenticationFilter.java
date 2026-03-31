package com.empsys.filter;

import com.empsys.service.JwtValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
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
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        
        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // ALL other endpoints require JWT Bearer token
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("JWT Bearer token required but missing for path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"JWT Bearer token required. Please provide Authorization header: Bearer <token>\"}");
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            String username = jwtValidationService.getUsernameFromToken(token);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("JWT Bearer token validated successfully for user: {} on path: {}", username, path);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT Bearer token validation failed for path {}: {}", path, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired JWT Bearer token\"}");
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

