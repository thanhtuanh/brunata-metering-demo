# Deploy auf Render (brunata-metering-demo.onrender.com)

Diese Anleitung beschreibt das Deployment der Anwendung nach Render.com via GitHub.

## Voraussetzungen
- GitHub‑Repository mit diesem Projekt (Hauptbranch z. B. `main`)
- Render‑Account

## Variante A: Render Blueprint (render.yaml)

1) In Render Dashboard: New + → Blueprint
2) Repository verbinden → dieses Repo wählen
3) Render liest `render.yaml` und erstellt:
   - PostgreSQL‑Datenbank `metering-db`
   - Web Service `brunata-metering-demo`
4) Deploy bestätigen.

Hinweise zur Blueprint‑Konfiguration (Docker Runtime)
- Runtime: `docker` mit `dockerfilePath: ./Dockerfile`
- Container setzt beim Start `SPRING_DATASOURCE_URL` aus `DATABASE_URL` (`postgres:` → `jdbc:postgresql:`)
- Healthcheck: `/actuator/health`
- Auto Deploy: aktiv, jeder Push auf den Hauptbranch triggert Deployment.

## Variante B: Manuell (ohne Blueprint)

1) Database → New → PostgreSQL → Name `metering-db` → Plan `Free` → Create
2) Web Service → New → Build & deploy from a Git repository → dieses Repo
3) Settings:
   - Environment: `Linux`
   - Runtime: `docker`
   - Dockerfile Path: `./Dockerfile`
   - Health Check Path: `/actuator/health`
  4) Environment Variables:
   - `DATABASE_URL` → „Add from“ → Database: `metering-db` → `connectionString`
   - `SPRING_FLYWAY_CONNECT_RETRIES=60` (DB-Startup-Glitches abfedern)
   - `SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT=60000`
   - `SPRING_DATASOURCE_USERNAME` → Database: `metering-db` → `user`
   - `SPRING_DATASOURCE_PASSWORD` → Database: `metering-db` → `password`
   - Optional: `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`
   - Optional: `INTEGRATION_ENABLED=false` (deaktiviert Sync‑Jobs)
   - Optional: `INTEGRATION_ENABLED=false` (deaktiviert Sync‑Jobs, wenn keine externen Mock‑Services erreichbar sind)

## Domäne & Verfügbarkeit
- Nach dem ersten Deploy: Service‑URL z. B. `https://brunata-metering-demo.onrender.com`
- Endpunkte:
  - Landing Page: `/`
  - OpenAPI UI: `/swagger-ui/index.html`
  - Health: `/actuator/health`
  - Prometheus: `/actuator/prometheus`

## Security/Compliance
- Keine Auth/RBAC: Die API ist aktuell ohne Authentifizierung/Autorisierung als öffentliche Demo verfügbar.
  - Hinweis: Nur synthetische Demodaten; keine personenbezogenen Daten, Logs ohne PII.
  - CORS restriktiv auf Demo-Domain gesetzt.
- Optional: Basic Auth aktivierbar über Env `DEMO_SECURITY_BASIC_ENABLED=true`.
  - Demo-User: `demo` / `demo123`
  - Öffentlich bleiben: `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/health`, `/actuator/prometheus`, statische Seiten.

## Troubleshooting
- Port‑Fehler: Dockerfile startet mit `--server.port=$PORT`.
- DB‑Verbindung: Falls Fehler „No suitable driver“ oder „invalid JDBC URL“, prüfe die `SPRING_DATASOURCE_URL`‑Transformation (postgres → jdbc:postgresql).
- Flyway Migrations: Läuft automatisch beim Start. Bei Problemen Logs prüfen („flyway“).
- Langsame Kaltstarts: Free‑Plan skaliert nach Inaktivität auf 0; erster Request dauert länger.
- Integration‑Calls schlagen fehl: Setze `INTEGRATION_ENABLED=false` oder konfiguriere `INTEGRATION_JIRA_BASE_URL`/`INTEGRATION_ERP_BASE_URL` passend.
 - Fehler „no main manifest attribute, in /app/app.jar“: Stelle sicher, dass beim Docker‑Build `spring-boot:repackage` ausgeführt wird (im Repo‑Dockerfile bereits enthalten). Alternativ lokal prüfen: `mvn -pl app -am package spring-boot:repackage`.

## CI/CD‑Hinweis
- AutoDeploy ist in `render.yaml` aktiviert. Jeder Push auf `main` triggert neuen Build/Deploy.
- Branch ändern? Im Render‑Dashboard den AutoDeploy‑Branch anpassen.
