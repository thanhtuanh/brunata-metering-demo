package com.brunata.meteringdemo.services.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfigurationswerte für Integrationsjobs (z. B. Jira-/ERP-Sync).
 *
 * Properties (application.yml):
 * integration.enabled: Master-Switch für alle Sync-Jobs
 * integration.jira-base-url: Basis-URL des Jira-Mock-Services
 * integration.erp-base-url: Basis-URL des ERP-Mock-Services
 * integration.offline-hours: Schwellenwert, ab wann ein Gerät als „offline“ gilt
 */
@ConfigurationProperties(prefix = "integration")
public record IntegrationProperties(
        boolean enabled,
        String jiraBaseUrl,
        String erpBaseUrl,
        int offlineHours
) {}
