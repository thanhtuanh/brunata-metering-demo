package com.brunata.meteringdemo.services.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.RoundingMode;

/**
 * Konfigurationswerte für die Abrechnung.
 *
 * Properties (application.yml):
 * billing.scale: Dezimalstellen für Betragsrundung (z. B. 2)
 * billing.rounding-mode: Rundungsmodus (z. B. HALF_UP)
 */
@ConfigurationProperties(prefix = "billing")
public record BillingProperties(int scale, RoundingMode roundingMode) {
}
