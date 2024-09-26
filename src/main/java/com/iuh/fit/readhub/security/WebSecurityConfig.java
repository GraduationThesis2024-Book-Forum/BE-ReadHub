package com.iuh.fit.readhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorize -> authorize
                        .requestMatchers("/api/v1/authen/**").permitAll()
                        .requestMatchers("/api/v1/book/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Vô hiệu hóa CSRF
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
