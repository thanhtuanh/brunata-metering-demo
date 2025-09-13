package com.brunata.meteringdemo.common;

/**
 * Domänenspezifische Validierungs-Exception (führt zu HTTP 400 im REST-Layer).
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
