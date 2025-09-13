package com.brunata.meteringdemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Fallback-Security-Konfiguration, wenn die Demo ohne Auth laufen soll.
 * Verhindert die Standard-Login-Seite von Spring Security, indem wir explizit
 * eine SecurityFilterChain definieren, die alles erlaubt.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "demo.security.basic-enabled", havingValue = "false", matchIfMissing = true)
public class SecurityDisabledConfig {

    @Bean
    SecurityFilterChain securityPermitAll(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }
}

