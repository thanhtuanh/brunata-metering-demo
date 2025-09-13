package com.brunata.meteringdemo.common;

import java.time.Instant;
import java.util.List;

/**
 * Einheitliches Fehlerobjekt für API-Antworten.
 * - error: kurzer Fehlercode
 * - details: Feldfehler/Validierungsfehler (optional)
 */
public record ApiError(Instant timestamp, String error, List<FieldError> details) {
    /** Einzelner Feldfehler (Name + Nachricht). */
    public record FieldError(String field, String message) {}
}
