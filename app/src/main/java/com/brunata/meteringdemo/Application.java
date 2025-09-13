package com.brunata.meteringdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Einstiegspunkt der Spring-Boot-Anwendung.
 * Auto-Konfiguration lädt Web, JPA, Flyway etc. gemäß Abhängigkeiten.
 */
@SpringBootApplication
@ConfigurationPropertiesScan({"com.brunata.meteringdemo.services.config"})
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
