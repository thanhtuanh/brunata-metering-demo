package com.brunata.meteringdemo.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import com.brunata.meteringdemo.common.ValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * API-spezifische Fehlerbehandlung für WebMVC-nahe Ausnahmen (Request-Parameter etc.).
 * Liegt im API-Modul, damit keine Servlet-Abhängigkeiten im common-Modul nötig sind.
 */
@RestControllerAdvice
public class ApiRestExceptionHandler {

    /** Ungültige/fehlgeschlagene Typkonvertierung bei Request-Parametern (z. B. LocalDate) → 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("BAD_REQUEST");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /** Fehlende Request-Parameter → 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("BAD_REQUEST");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /** Domänenvalidierung (z. B. monotone Zählerstände) → 400. */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleDomainValidation(ValidationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("BAD_REQUEST");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /** Bean Validation (RequestBody DTOs) → 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("BAD_REQUEST");
        pd.setDetail("Validation failure");
        return pd;
    }
}
