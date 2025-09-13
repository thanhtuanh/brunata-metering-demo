# Multi-stage build for Brunata Metering Demo

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY persistence/pom.xml persistence/pom.xml
COPY services/pom.xml services/pom.xml
COPY api/pom.xml api/pom.xml
COPY app/pom.xml app/pom.xml
COPY common common
COPY domain domain
COPY persistence persistence
COPY services services
COPY api api
COPY app app

# Build only the app artifact (skipping tests to speed up container build)
# Ensure Spring Boot repackage runs so the JAR is executable with a Main-Class
# Use fully qualified plugin goal with explicit version to avoid prefix resolution issues
# Build the full reactor once; app module will produce a bootable jar (repackage bound)
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /workspace/app/target/app-0.1.0.jar /app/app.jar

# Prepare entrypoint script to transform DATABASE_URL -> SPRING_DATASOURCE_URL
RUN set -eux; \
  cat > /app/entrypoint.sh <<'SH'
#!/bin/sh
set -eu
echo "Original DATABASE_URL=${DATABASE_URL-}"

JDBC_URL=""
if [ -n "${DATABASE_URL-}" ]; then
  JDBC_URL=$(printf %s "$DATABASE_URL" \
    | sed -E 's#^postgres(ql)?://#jdbc:postgresql://#; s#//[^/@]+@#//#')
fi

# Fallback: wenn der Host nicht extern aussieht, aus DB_HOST/DB_PORT/DB_NAME bauen
case "$JDBC_URL" in
  *render.com*|*renderusercontent.com*) : ;; # extern ok
  *)
    if [ -n "${DB_HOST-}" ] && [ -n "${DB_PORT-}" ] && [ -n "${DB_NAME-}" ]; then
      JDBC_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
    fi
  ;;
esac

# sslmode=require anhängen, falls fehlend
case "${JDBC_URL:-}" in
  *\?*) : ;;
  "") : ;; # leer: nichts tun
  *) JDBC_URL="${JDBC_URL}?sslmode=require" ;;
esac
case "${JDBC_URL:-}" in
  *sslmode=*) : ;;
  *\?*) JDBC_URL="${JDBC_URL}&sslmode=require" ;;
esac

if [ -n "${JDBC_URL:-}" ]; then
  export SPRING_DATASOURCE_URL="$JDBC_URL"
  echo "Using JDBC_URL=$JDBC_URL"
fi

exec java $JAVA_OPTS -jar /app/app.jar --server.port="${PORT:-8080}"
SH
RUN chmod +x /app/entrypoint.sh

# Render provides PORT; ensure container exposes default for local runs
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
