# Brunata Metering Demo

Java-Backend-Demo für Zählerdaten und Abrechnung – mit klarer Modularchitektur, Flyway-Migrationen und schnellen Dev-Skripten.
Validiert typische Metering-Flows (Geräte/Readings → Abrechnung) mit klaren Schichten (API→Service→Persistence) und robusten Migrationen (Flyway). Health, Swagger und Prometheus sind out-of-the-box aktiv, wodurch Betrieb & Diagnose sofort möglich sind.

Live-Demo (Render): https://brunata-metering-demo.onrender.com

OpenAPI UI: https://brunata-metering-demo.onrender.com/swagger-ui/index.html
OpenAPI JSON: https://brunata-metering-demo.onrender.com/v3/api-docs
Health: https://brunata-metering-demo.onrender.com/actuator/health
Prometheus: https://brunata-metering-demo.onrender.com/actuator/prometheus

Dokumentation
- [Schritt‑für‑Schritt Tutorial (Technik‑Doku)](docs/tutorial.md)
- [Entwickler‑Doku (Architektur, Setup, Datenmodell, Tests)](readme-dev.md)
- [Deployment auf Render (GitHub Auto‑Deploy)](docs/render-deploy.md)

## Tech-Stack
- Java 21, Maven (Multi-Module)
- Spring Boot 3.3.x (Web, Actuator)
- Spring Data JPA (Hibernate)
- PostgreSQL 16 (Docker Compose)
- Flyway 10 (plus `flyway-database-postgresql`)
- OpenAPI (springdoc-openapi-starter-webmvc-ui)
- WebClient (reactive) mit Timeouts/Retry
- Tests: JUnit 5, Mockito, MockMvc; (Empfehlung: Testcontainers Postgres)

## Architektur
- Schichten:
  - API: dünne REST-Controller; Validierung der Eingaben
  - Services: Geschäftslogik, Transaktionen, Fehler (ValidationException)
  - Persistence: Spring-Data-Repositories, performante Queries
  - Domain: JPA-Entities (Device, MeterReading, Tariff, Contract, Invoice)
  - Common: ApiError, Exception-Handling
- Migrations: ausschließlich via Flyway (`V*_*.sql`), keine nachträglichen Änderungen angewandter Migrationen
- Observability: Actuator (Health, Metrics)

### Architekturdiagramm (einfaches Schema)

```
          Clients (Swagger UI, cURL)
                    |
                    v
        +-------------------------+
        | api (REST Controller)   |  <— OpenAPI
        +-----------+-------------+
                    |
                    v
        +-------------------------+
        | services (Business)     |  — Validierung, Transaktionen,
        |                         |    Fehler (ValidationException)
        +-----------+-------------+
                    |
                    v
        +-------------------------+          +----------------------+
        | persistence (Repos)     |  JPA     | PostgreSQL 16        |
        | Spring Data JPA         +<-------->+ (Flyway Schema)      |
        +-----------+-------------+          +----------------------+
                    |
                    v
        +-------------------------+
        | domain (Entities)       |
        +-------------------------+

        +-------------------------+          +----------------------+
        | common (ApiError etc.)  |<—— Fehler-Mapping (ProblemDetail)
        +-------------------------+

        +-------------------------+          +----------------------+
        | app (Runtime)           |———→ Flyway Migrationen
        | Spring Boot + Actuator  |———→ Health/Metrics
        +-------------------------+          +----------------------+

        +-------------------------+          +----------------------+
        | WebClient (Integr.)     |———→ Jira/ERP (Mock)
        +-------------------------+          +----------------------+
```

### Architekturmodell: Modularer Monolith
- Einzelnes deploybares Spring‑Boot‑Artefakt (`app`).
- Klare Modulgrenzen (Maven): `api`, `services`, `persistence`, `domain`, `common`.
- Vorteile: einfache Deployments, konsistente Transaktionen, schnelle lokale Entwicklung.

## Module
- `common`: Fehlermodell (ApiError), `ValidationException`, `RestExceptionHandler`
- `domain`: JPA-Entities (Device, MeterReading, Tariff, Contract, Invoice)
- `persistence`: Repositories (inkl. first/last‑Reading im Zeitraum)
- `services`: Geschäftslogik (Readings-Validierung, Billing)
- `api`: REST-Controller (Readings, Billing) + OpenAPI
- `app`: Spring Boot Starter (Actuator, Flyway) + Ressourcen (Flyway-SQL)

## Datenmodell (Kurz)
- `device(id, type, serial_no, location, last_seen_at, status)`
- `meter_reading(id, device_id, reading_time, value, unit, source)`
  - Index: `(device_id, reading_time)`
- `tariff(id, name, price_per_unit, unit)`
- `contract(id, customer_name, device_id, start_date, end_date, tariff_id)`
- `invoice(id, contract_id, period_from, period_to, consumption, amount, status, created_at)`

## REST-APIs (Auszug)
- `POST /api/readings` – Liste von Messwerten anlegen (DTO-validiert)
- `GET  /api/readings?deviceId=UUID` – Messwerte eines Geräts auflisten (Demo)
- `POST /api/billing/run?contractId=UUID&from=YYYY-MM-DD&to=YYYY-MM-DD` – Rechnung erzeugen

## Sync & Integrationen (Mock)
- Geplante Jobs via `@EnableScheduling`:
  - Jira-Meldung offline Geräte: alle 5 Minuten
  - ERP-Kundensync (Demo): alle 10 Minuten
- Konfiguration (`application.yml`):
  - `integration.enabled`, `integration.offlineHours`, `integration.jiraBaseUrl`, `integration.erpBaseUrl`
- Implementierung/Performance:
  - Offline‑Selektion DB‑seitig (Repository) statt Full‑Scan
  - Nicht‑blockierende Pipeline (`Flux.flatMap`) mit begrenzter Parallelität, Timeout und Backoff‑Retry (nur 5xx)
  - Globaler `WebClient` mit Connect/Response‑Timeouts: `services/.../config/WebClientConfig`
  - Zusätzliche Indizes für Offline‑Checks: `app/.../db/migration/V7__device_indexes.sql`

## Schnellstart (lokal)
```bash
# Einfache Skripte
./start-demo.sh        # startet DB, Flyway-Repair (Dev), Build & App
./stop-demo.sh         # stoppt App & DB (Container bleibt)
./stop-demo.sh --down  # stoppt App, entfernt DB + Volumes
```

Voraussetzungen: JDK 21, Maven 3.9+, Docker & Docker Compose.

Manuell (alternativ):
```bash
docker compose up -d db
mvn -U -DskipTests clean install
mvn -pl app spring-boot:run
```

Health: https://brunata-metering-demo.onrender.com/actuator/health

## Deployment (Render)
- Beschreibung und Blueprint: siehe `docs/render-deploy.md` und `render.yaml`
- Produktiv‑URL (Demo): https://brunata-metering-demo.onrender.com

## Beispiele (cURL)
```bash
# Reading posten (ein Messwert)
curl -X POST https://brunata-metering-demo.onrender.com/api/readings \
 -H 'Content-Type: application/json' \
 -d '[{"deviceId":"<UUID>","readingTime":"2025-09-12T10:00:00Z","value":123.45,"unit":"kWh","source":"LoRa"}]'

# Billing (erfordert Contract + Readings im Zeitraum)
curl -X POST "https://brunata-metering-demo.onrender.com/api/billing/run?contractId=<CONTRACT_UUID>&from=2025-09-01&to=2025-09-30"
```

### Auth & Sicherheit
- Demo-Daten; keine echten Kundendaten. Logs ohne PII.
- CORS restriktiv (nur Demo-Domain).
- Auth-Varianten:
  - Basic (Demo-User) oder
  - JWT via `/auth/login` + "Authorize" in Swagger UI.
- Health/Metrics: `/actuator/health`, `/actuator/prometheus` (keine sensiblen Details).

## Tests & Checks
- Nur API-Tests: `./scripts/test-api.sh`
- Nur Services-Tests: `./scripts/test-services.sh`
- Voller Build + Unit-Tests: `./scripts/test-all.sh`
- CI-ähnlich (lokal): `./scripts/test-ci.sh`
- Integrationstest (Testcontainers): `./scripts/test-integration.sh`
- Build-Check (ohne Tests): `./scripts/check-build.sh`
- DB‑Health (Compose): `./scripts/check-db.sh`
- Flyway Status/Repair: `./scripts/check-flyway.sh`, `./scripts/repair-flyway.sh`
- Metrics: `./scripts/check-metrics.sh`

## Troubleshooting
- Flyway „checksum mismatch“ (z. B. nach Edit einer bereits angewendeten Migration):
  - Dev: `./scripts/repair-flyway.sh` oder `./stop-demo.sh --down` und neu starten
  - Grundsatz: angewendete Migrationen nicht ändern; neue `V<N>__...sql` anlegen
- Java Textblocks in JPQL: nach `"""` muss eine neue Zeile folgen
- Swagger UI: Pfad ist `/swagger-ui/index.html`

## Known Limits (MVP)
- Auth/RBAC vereinfacht: Demo-User/Basic; kein Keycloak/JWT-Rollenmodell.
- Billing vereinfacht: lineares Tarifmodell; keine Staffel-/Zeitfensterpreise.
- Idempotenz fehlt: POST /readings kann bei Retries doppelt erzeugen.
- Rate Limiting / Throttling fehlt: kein Schutz gegen Burst-Loads.
- Multi-Tenancy/Scopes: (noch) nicht mandantenfähig.
- Datenvalidierung/basic: Plausibilitäten einfach; kein Schema-Level für Einheiten/Konversion.
- Pagination/Filter: rudimentär (page,size); komplexe Filter (by device/time) nur Basis.
- Tests: Fokus auf Happy Path + wenige Fehlerfälle; Last-/Contract-Tests fehlen.
- Observability: Metriken vorhanden, aber keine fertigen Dashboards/Alerts.
- SLOs/SLIs: nicht definiert; kein Error Budget/Availability-Ziel.

## Next Steps (Roadmap)
- Security/RBAC: Keycloak + JWT, Rollen (viewer/ops/admin), Swagger-SecuritySchemes.
- API-Versionierung: `/api/v1/...`; Backward-Compatibility Leitlinien.
- Idempotenz: Idempotency-Key für Write-APIs, 409/200 Semantik.
- Rate Limiting: Token Bucket/Redis, 429-Handling + Retry-After.
- Billing-Engine: Tarifzonen, Staffelpreise, Zeitfenster, Rundungsregeln, Audit-Log.
- Readings-Ingest @Scale: Bulk-Endpoints, asynchron (Kafka/Queue), DLQ.
- Validierung: Bean Validation + domänenspez. Plausibilität (Zeitraum, Einheit).
- Query/Reporting: erweiterte Filter (deviceId, range), Sortierung, Export.
- Tests: Integration (Fehlerpfade), Contract (OpenAPI), e2e (Postman), Performance (Gatling/JMH).
- Observability+: Grafana-Dashboards, Alerting (latency, 5xx, DB-Errors), Trace-IDs in Logs.
- DB & Performance: Indizes (device_id,timestamp), Partitionierung, Caching (Caffeine/Redis).
- Deploy: Blue/Green/Rolling, Migrations-Checks in CI/CD.
- Compliance: Audit-Events, Lösch-/Anonymisierungsroutinen, DSGVO-Hinweise im README.
- DX: Postman-Collection, Beispieldaten, Makefile (build/run), Status-Badges.

### Demo-Check (vor jedem Termin, 60 Sek.)
1. `GET /actuator/health` → UP
2. `GET /v3/api-docs` (lädt flott)
3. Swagger öffnen → `POST /api/readings` (Demo-Eintrag)
4. `POST /api/billing/run` → Response zeigt `consumption` & `amount`
5. Optional: `GET /actuator/prometheus` kurz zeigen
