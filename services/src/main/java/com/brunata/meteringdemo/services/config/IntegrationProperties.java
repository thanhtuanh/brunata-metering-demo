package com.brunata.meteringdemo.services.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration")
public record IntegrationProperties(
        boolean enabled,
        String jiraBaseUrl,
        String erpBaseUrl,
        int offlineHours
) {}

