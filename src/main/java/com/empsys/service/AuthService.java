package com.empsys.service;

import com.empsys.dto.LoginRequestDTO;
import com.empsys.entity.UserCreds;
import com.empsys.exception.InvalidCredentialsException;
import com.empsys.exception.ResourceNotFoundException;
import com.empsys.repository.UserCredsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserCredsRepository userCredsRepository;

    public String login(LoginRequestDTO loginDTO) {
        log.info("Login attempt for user: {}", loginDTO.getUsername());
        UserCreds user = userCredsRepository.findByUsername(loginDTO.getUsername());

        // Username not found
        if (user == null) {
            log.warn("Login failed - User not found: {}", loginDTO.getUsername());
            throw new ResourceNotFoundException("User not found with username: " + loginDTO.getUsername());
        }

        // Incorrect password
        if (!user.getPasswordHash().equals(loginDTO.getPassword())) {
            log.warn("Login failed - Invalid password for user: {}", loginDTO.getUsername());
            throw new InvalidCredentialsException("Invalid password!");
        }
        log.info("Login successful for user: {}", loginDTO.getUsername());
        return "Login successful!";
    }
}
