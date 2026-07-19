package com.aw.workflow.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class CamundaSecurityConfig {

    @Bean
    public SecurityFilterChain camundaSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/camunda/**")
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // Tắt tuyệt đối Popup
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}