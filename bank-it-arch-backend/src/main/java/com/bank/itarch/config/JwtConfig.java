package com.bank.itarch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret = "bank-it-arch-jwt-secret-key-2024";
    private Long expiration = 86400000L;
    private String tokenPrefix = "Bearer ";
    private String headerName = "Authorization";
}
