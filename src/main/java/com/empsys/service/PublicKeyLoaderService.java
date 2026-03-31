package com.empsys.service;

import com.empsys.dto.PublicKeyResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private RestTemplate restTemplate;
    
    @PostConstruct
    public void loadPublicKey() {
        try {
            logger.info("Loading public key from: {}{}", authServiceUrl, publicKeyEndpoint);
            String url = authServiceUrl + publicKeyEndpoint;
            PublicKeyResponse response = restTemplate.getForObject(url, PublicKeyResponse.class);
            
            if (response != null && response.getPublicKey() != null) {
                logger.info("✓ Public key fetched successfully from auth service");
                this.publicKey = parsePublicKey(response.getPublicKey());
                logger.info("✓ Public key loaded and parsed successfully. Algorithm: {}", response.getAlgorithm());
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

