package com.creditapp.shared.config;

import com.creditapp.auth.filter.JwtAuthenticationFilter;
import com.creditapp.shared.service.JwtTokenService;
import com.creditapp.shared.security.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                          CustomAccessDeniedHandler accessDeniedHandler,
                                          ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/health/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler));
        
        // Only add JWT filter if available (not in all test profiles)
        jwtFilterProvider.ifAvailable(filter -> {
            try {
                http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }
}
