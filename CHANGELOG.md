# Changelog

## [v0.1.0] - 2025-09-13

### Added
- Live API-Demo (Spring Boot + PostgreSQL) auf Render:
  - Base: https://brunata-metering-demo.onrender.com
  - Swagger UI: `/swagger-ui/index.html`
  - OpenAPI JSON: `/v3/api-docs`
  - Health: `/actuator/health`, Metrics: `/actuator/prometheus`
- Domänenmodell Meter/Device, Readings und Billing (Verbrauch + Betrag).
- Flyway-Baseline + Demo-Seed (synthetische Daten).
- cURL-Generator & Beispiel-Requests (Readings ingest, Billing run).
- Observability: Spring Boot Actuator, Prometheus-Endpunkt.
- Docker-Support (lokal) und Render-Deployment.

### Changed
- README überarbeitet: Live-Links, Quickstart (cURL), API-Übersicht.
- Konsistente Bezeichner & Endpunkte (Swagger ↔ README ↔ PDF).

### Security/Compliance
- Aktuell keine Auth/RBAC (öffentliche Demo).
- Demo-Daten; keine echten Kundendaten. Logs ohne PII. CORS restriktiv.

### Known Limits (siehe README)
- Vereinfacht (MVP): Auth/RBAC basic, Billing-Formel simpel, Idempotenz & Ratenbegrenzung fehlen noch.
