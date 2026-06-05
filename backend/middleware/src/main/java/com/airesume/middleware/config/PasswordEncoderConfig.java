package com.airesume.middleware.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    // Declared as a @Bean so Spring can inject it anywhere via @Autowired.
    // Strength 12 = BCrypt work factor — higher means slower hash (harder to brute-force),
    // 12 is the production-safe default; drop to 10 if startup feels slow in dev.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
