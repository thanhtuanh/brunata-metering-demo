package com.brunata.meteringdemo.services.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.RoundingMode;

@ConfigurationProperties(prefix = "billing")
public record BillingProperties(int scale, RoundingMode roundingMode) {
}

