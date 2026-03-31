package com.empsys.controller;

import com.empsys.dto.LoginRequestDTO;
import com.empsys.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginDTO) {
        log.info("Login API called for username: {}", loginDTO.getUsername());
        try {
            String message = authService.login(loginDTO);
            log.info("User '{}' logged in successfully", loginDTO.getUsername());
            return ResponseEntity.ok(message);

        } catch (Exception ex) {
            log.error("Login failed for user '{}': {}", loginDTO.getUsername(), ex.getMessage());
            throw ex; // Let global exception handler handle it
        }
    }
}
