package com.brunata.meteringdemo;

import com.brunata.meteringdemo.persistence.DeviceRepository;
import com.brunata.meteringdemo.persistence.MeterReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AppIntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>(
            System.getenv().getOrDefault("TEST_POSTGRES_IMAGE", "postgres:16")
    )
            .withDatabaseName(System.getenv().getOrDefault("TEST_DB_NAME", "metering"))
            .withUsername(System.getenv().getOrDefault("TEST_DB_USER", "metering"))
            .withPassword(System.getenv().getOrDefault("TEST_DB_PASSWORD", "metering"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r){
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired DeviceRepository deviceRepo;
    @Autowired MeterReadingRepository readingRepo;

    @Test
    void context_and_flyway_migrations_load() {
        // Wenn der Kontext da ist und Repositories verfügbar sind, sind Migrationen gelaufen
        assertThat(deviceRepo).isNotNull();
        assertThat(readingRepo).isNotNull();
    }
}
