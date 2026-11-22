# Multi-stage build for LockMySeat Plain-Java backend
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app
COPY backend/ /app/backend/

# Compile all Java sources
RUN mkdir -p /app/backend/bin && \
    javac -cp "/app/backend/lib/*:/app/backend/src" \
    -d /app/backend/bin \
    $(find /app/backend/src -name "*.java")

# Final runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=build /app/backend/bin /app/backend/bin
COPY --from=build /app/backend/lib /app/backend/lib
COPY backend/config.properties.example /app/backend/config.properties

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -cp '/app/backend/bin:/app/backend/lib/*' Main"]
