package com.brunata.meteringdemo.api;

import com.brunata.meteringdemo.common.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

/**
 * API-spezifische Fehlerbehandlung für WebMVC-nahe Ausnahmen (Request-Parameter etc.).
 * Vereinheitlicht das Fehlerformat auf ApiError.
 */
@RestControllerAdvice
public class ApiRestExceptionHandler {

    /** Ungültige/fehlgeschlagene Typkonvertierung bei Request-Parametern (z. B. LocalDate) → 400 (ApiError). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var err = new ApiError(
                Instant.now(),
                "BAD_REQUEST",
                List.of(new ApiError.FieldError(ex.getName(), ex.getMessage()))
        );
        return ResponseEntity.badRequest().body(err);
    }

    /** Fehlende Request-Parameter → 400 (ApiError). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        var err = new ApiError(
                Instant.now(),
                "BAD_REQUEST",
                List.of(new ApiError.FieldError(ex.getParameterName(), ex.getMessage()))
        );
        return ResponseEntity.badRequest().body(err);
    }
}
