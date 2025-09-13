package com.brunata.meteringdemo.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReadingDto(
        @NotNull UUID deviceId,
        @NotNull @PastOrPresent Instant readingTime,
        @NotNull @PositiveOrZero BigDecimal value,
        @NotBlank String unit,
        @NotBlank String source
) {}
