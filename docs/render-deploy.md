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

Hinweise zur Blueprint‑Konfiguration
- Build: `mvn -q -DskipTests -pl app -am package`
- Start: Wrapper setzt `SPRING_DATASOURCE_URL` aus `DATABASE_URL` (`postgres:` → `jdbc:postgresql:`) und startet das JAR.
- Healthcheck: `/actuator/health`
- Auto Deploy: aktiv, jeder Push auf den Hauptbranch triggert Deployment.

## Variante B: Manuell (ohne Blueprint)

1) Database → New → PostgreSQL → Name `metering-db` → Plan `Free` → Create
2) Web Service → New → Build & deploy from a Git repository → dieses Repo
3) Settings:
   - Environment: `Linux`
   - Build Command: `mvn -q -DskipTests -pl app -am package`
   - Start Command:
     ```bash
     bash -lc 'export SPRING_DATASOURCE_URL=$(echo "$DATABASE_URL" | sed -E "s/^postgres:/jdbc:postgresql:/");
     java -jar app/target/app-0.1.0.jar --server.port=$PORT'
     ```
   - Health Check Path: `/actuator/health`
  4) Environment Variables:
   - `JAVA_VERSION=21`
   - `DATABASE_URL` → „Add from“ → Database: `metering-db` → `connectionString`
   - `SPRING_DATASOURCE_USERNAME` → Database: `metering-db` → `user`
   - `SPRING_DATASOURCE_PASSWORD` → Database: `metering-db` → `password`
   - Optional: `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`
   - Optional: `INTEGRATION_ENABLED=false` (deaktiviert Sync‑Jobs, wenn keine externen Mock‑Services erreichbar sind)

## Domäne & Verfügbarkeit
- Nach dem ersten Deploy: Service‑URL z. B. `https://brunata-metering-demo.onrender.com`
- Endpunkte:
  - Landing Page: `/`
  - OpenAPI UI: `/swagger-ui/index.html`
  - Health: `/actuator/health`
  - Prometheus: `/actuator/prometheus`

## Troubleshooting
- Port‑Fehler: Start mit `--server.port=$PORT` sichert den richtigen Render‑Port.
- DB‑Verbindung: Falls Fehler „No suitable driver“ oder „invalid JDBC URL“, prüfe die `SPRING_DATASOURCE_URL`‑Transformation (postgres → jdbc:postgresql).
- Flyway Migrations: Läuft automatisch beim Start. Bei Problemen Logs prüfen („flyway“).
- Langsame Kaltstarts: Free‑Plan skaliert nach Inaktivität auf 0; erster Request dauert länger.
 - Integration‑Calls schlagen fehl: Setze `INTEGRATION_ENABLED=false` oder konfiguriere `INTEGRATION_JIRA_BASE_URL`/`INTEGRATION_ERP_BASE_URL` passend.

## CI/CD‑Hinweis
- AutoDeploy ist in `render.yaml` aktiviert. Jeder Push auf `main` triggert neuen Build/Deploy.
- Branch ändern? Im Render‑Dashboard den AutoDeploy‑Branch anpassen.
