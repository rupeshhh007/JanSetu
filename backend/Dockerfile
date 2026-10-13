# JanSetu Spring Boot API: reproducible Java 21 multi-stage container.
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-noble
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build --chown=app:app /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/jansetu.jar
USER app
ENV SPRING_PROFILES_ACTIVE=production
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/jansetu.jar"]
