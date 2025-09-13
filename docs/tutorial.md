# Brunata Metering Demo – Schritt‑für‑Schritt Tutorial

Dieses Tutorial führt durch Setup, erste Requests und die wichtigsten Komponenten.

## 1) Start lokal
```bash
./start-demo.sh
```
- Startet Postgres (Docker Compose), wartet auf „healthy“
- Führt optional `flyway:repair` aus (Dev‑Helfer)
- Baut alle Module (Tests übersprungen) und startet die App
- Wählt automatisch einen freien Port (8080..8085)
 - Führt beim Start alle Flyway‑Migrationen inkl. `V7__device_indexes.sql` aus

Öffne anschließend:
- Landing Page: `http://localhost:<port>/`
- Swagger: `http://localhost:<port>/swagger-ui/index.html`
- Health: `http://localhost:<port>/actuator/health`

## 2) Demo‑Daten
- Flyway legt Seed‑Daten automatisch an (deterministische UUIDs)
- Device: `62eb5088-15b6-4128-b7fe-44690e42099d`
- Contract: `f70ca20d-a2f3-4d87-ab64-482fe327d4c4`

## 3) Erste Requests (Swagger oder cURL)
- `POST /api/readings` – Messwertliste posten (Zeit nicht in der Zukunft; Wert ≥ letztem Zählerstand)
- `GET /api/readings?deviceId=...` – Messwerte anzeigen
- `POST /api/billing/run?contractId=...&from=YYYY-MM-DD&to=YYYY-MM-DD` – Rechnung erzeugen

Hinweis (optional Basic Auth): Wenn die Demo mit Basic Auth gestartet wurde, `-u demo:demo123` bei cURL anhängen und im Browser die Anmeldedaten verwenden.

## 4) Architektur‑Überblick
- Module: common, domain, persistence, services, api, app
- Schichten: API → Services → Persistence
- Datenbank: PostgreSQL 16, Migrationen via Flyway
 - Sync: reaktive Pipeline mit Timeout/Retry und DB‑seitiger Offline‑Selektion

Details für Entwickler: siehe `readme-dev.md`.

## 5) Deployment auf Render
- URL: `https://brunata-metering-demo.onrender.com`
- Anleitung und Blueprint: `docs/render-deploy.md`, `render.yaml`

Hinweis: Falls keine externen Mock‑Services für Jira/ERP verfügbar sind, kann der Sync deaktiviert werden (Env‑Var `INTEGRATION_ENABLED=false`).

## 6) Bezug zur Stellenbeschreibung (Java Backend)
- Saubere Schichten: Klare Trennung API → Service → Persistence mit Spring Boot 3.3.
- Stabiler Datenlayer: PostgreSQL 16, JPA, Flyway‑Migrationen, sinnvolle Indizes und Constraints.
- Fachlogik nachvollziehbar: Abrechnung mit DB‑Aggregation (min/max), Idempotenz, Transaktionen, validierte Eingaben.
- Integrationserfahrung: Geplante Jobs (Scheduling), resiliente HTTP‑Aufrufe via WebClient (Timeout, Retry, Budget), Konfiguration via `@ConfigurationProperties`.
- Moderne Java‑Basis: Java 21, Records für Config‑Typen, reaktive Pipelines, Option für virtuelle Threads.
- Qualität & Tests: Unit/Controller‑Tests, präzise Validations, OpenAPI/Swagger.
- Dev‑Ergonomie: Skripte (`start-demo.sh`), Docker‑Compose für DB, ausführliche README/Tutorials.

## 7) Auth & Sicherheit (optional)
- Keine Auth/RBAC: Die API ist aktuell ohne Authentifizierung/Autorisierung als öffentliche Demo verfügbar.
  - Hinweis: Nur synthetische Demodaten; keine personenbezogenen Daten, Logs ohne PII.
  - CORS restriktiv auf Demo‑Domain gesetzt.
- Optionale Basic Auth aktivieren mit Property/Env `demo.security.basic-enabled=true` (ENV: `DEMO_SECURITY_BASIC_ENABLED=true`).
- Demo‑Zugang: Benutzer `demo`, Passwort `demo123`.
- Öffentlich bleiben: `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/health`, `/actuator/prometheus`, statische Seiten (`/`, `/index.html`, `/dev.html`).
