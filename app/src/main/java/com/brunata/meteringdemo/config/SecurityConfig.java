package com.brunata.meteringdemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Optionale, minimale Basic-Auth. Aktivierung über Property:
 * demo.security.basic-enabled=true
 *
 * Berechtigungen:
 * - Öffentlich: Landing-Page/Static, OpenAPI/Swagger, Health, Prometheus
 * - Authentifiziert: alle übrigen Endpunkte
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "demo.security.basic-enabled", havingValue = "true")
public class SecurityConfig {

    @Value("${demo.cors.allowed-origins:https://brunata-metering-demo.onrender.com,http://localhost:8080,http://localhost:8081,http://localhost:8082}")
    private String corsAllowedOrigins;

    @Value("${demo.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS,HEAD}")
    private String corsAllowedMethods;

    @Value("${demo.cors.allowed-headers:*}")
    private String corsAllowedHeaders;

    @Value("${demo.cors.allow-credentials:true}")
    private boolean corsAllowCredentials;

    @Value("${demo.cors.max-age:3600}")
    private long corsMaxAgeSeconds;

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "", "/index", "/index.html", "/dev.html",
                                "/static/**", "/assets/**", "/error"
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-ui.html", "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health", "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(h -> h.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        UserDetails demo = User.withUsername("demo")
                .password(encoder.encode("demo123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(demo);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(splitAndTrim(corsAllowedOrigins));
        cfg.setAllowedMethods(splitAndTrim(corsAllowedMethods));
        cfg.setAllowedHeaders(splitAndTrim(corsAllowedHeaders));
        cfg.setAllowCredentials(corsAllowCredentials);
        cfg.setMaxAge(corsMaxAgeSeconds); // Preflight Cache
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private List<String> splitAndTrim(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
