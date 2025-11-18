package com.se100.bds.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

import java.util.Locale;

@Configuration
public class PayOSConfig {
    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.log-level}")
    private String logLevel;

    @Bean(name = "payOSPaymentClient")
    public PayOS payOSPaymentClient() {
        ClientOptions options = ClientOptions.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .checksumKey(checksumKey)
                .logLevel(parseLogLevel(logLevel))
                .build();
        return new PayOS(options);
    }

    private ClientOptions.LogLevel parseLogLevel(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return ClientOptions.LogLevel.NONE;
        }
        try {
            return ClientOptions.LogLevel.valueOf(candidate.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            return ClientOptions.LogLevel.NONE;
        }
    }
}
