package com.empsys.dto;

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

