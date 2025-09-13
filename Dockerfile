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

# Render provides PORT and DATABASE_URL. Transform DATABASE_URL to JDBC on start.
EXPOSE 8080
CMD ["bash","-lc","echo Original DATABASE_URL=$DATABASE_URL; JDBC_URL=$(printf %s \"$DATABASE_URL\" | sed -E 's#^postgres(ql)?://#jdbc:postgresql://#; s#//[^/@]+@#//#'); echo Using JDBC_URL=$JDBC_URL; export SPRING_DATASOURCE_URL=\"$JDBC_URL\"; exec java $JAVA_OPTS -jar /app/app.jar --server.port=$PORT"]
