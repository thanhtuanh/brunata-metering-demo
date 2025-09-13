package com.brunata.meteringdemo.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
// (keine spring-webmvc-abhängigen Imports im common-Modul)
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Globale REST-Fehlerbehandlung.
 * Mapped Domänen-/Bean-Validierungsfehler auf einheitliches ApiError-Format.
 */
@ControllerAdvice
public class RestExceptionHandler {

    /** Mappt domänenspezifische ValidationException auf 400 + ApiError. */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        var err = new ApiError(Instant.now(), "VALIDATION_ERROR",
                java.util.List.of(new ApiError.FieldError("generic", ex.getMessage())));
        return ResponseEntity.badRequest().body(err);
    }

    /** Mappt Bean Validation (jakarta.validation) auf 400 + Feldfehlerliste. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBeanValidation(MethodArgumentNotValidException ex){
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.FieldError(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        var err = new ApiError(Instant.now(), "VALIDATION_ERROR", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /** Request-Body nicht lesbar (z. B. ungültiges JSON/UUID) → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("BAD_REQUEST");
        var msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        pd.setDetail(msg);
        return pd;
    }

    // Parameter-Fehler behandelt api.ApiRestExceptionHandler (liegt im API-Modul mit WebMVC-Abhängigkeiten).

    // Fehlende/ungültige Request-Parameter werden vom MVC-Default als 400 gemeldet.

    // 404-Handling verbleibt beim Default-Mechanismus, um WebMVC-Abhängigkeiten im common-Modul zu vermeiden.

    /**
     * Fallback auf RFC 7807 ProblemDetail für unerwartete Fehler (Spring Boot 3).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("INTERNAL_ERROR");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}
