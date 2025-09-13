# Brunata Metering Demo

Java-Backend-Demo für Zählerdaten und Abrechnung – mit klarer Modularchitektur, Flyway-Migrationen und schnellen Dev-Skripten.

Live-Demo (Render): https://brunata-metering-demo.onrender.com

OpenAPI UI lokal: http://localhost:8080/swagger-ui/index.html

Dokumentation
- docs/tutorial.md – Schritt‑für‑Schritt Tutorial (Technik‑Doku)
- readme-dev.md – Entwickler‑Doku (Architektur, Setup, Datenmodell, Tests)
- docs/render-deploy.md – Deployment auf Render (GitHub Auto‑Deploy)

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

Health: http://localhost:8080/actuator/health

## Deployment (Render)
- Beschreibung und Blueprint: siehe `docs/render-deploy.md` und `render.yaml`
- Produktiv‑URL (Demo): https://brunata-metering-demo.onrender.com

## Beispiele (cURL)
```bash
# Device anlegen (psql‑Beispiel)
# insert into device(id, type, serial_no, location) values(gen_random_uuid(),'HEAT','HT-001','Munich');

# Reading posten (ein Messwert)
curl -X POST http://localhost:8080/api/readings \
 -H 'Content-Type: application/json' \
 -d '[{"deviceId":"<UUID>","readingTime":"2025-09-12T10:00:00Z","value":123.45,"unit":"kWh","source":"LoRa"}]'

# Billing (erfordert Contract + Readings im Zeitraum)
curl -X POST "http://localhost:8080/api/billing/run?contractId=<CONTRACT_UUID>&from=2025-09-01&to=2025-09-30"
```

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

## Roadmap
- ERP/Jira‑Sync (Mock) via WebClient + Scheduler
- Security (Keycloak/OIDC), Rollen & Endpoint‑Absicherung
- Integrationstests (Testcontainers Postgres), Metriken (Prometheus/Grafana)
