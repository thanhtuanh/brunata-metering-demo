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
RUN mvn -q -DskipTests -pl app -am package \
 && mvn -q -pl app org.springframework.boot:spring-boot-maven-plugin:3.3.2:repackage

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /workspace/app/target/app-0.1.0.jar /app/app.jar

# Render provides PORT and DATABASE_URL. Transform DATABASE_URL to JDBC on start.
EXPOSE 8080
CMD ["bash","-lc","export SPRING_DATASOURCE_URL=$(echo \"$DATABASE_URL\" | sed -E 's/^postgres:/jdbc:postgresql:/'); exec java $JAVA_OPTS -jar /app/app.jar --server.port=$PORT"]
