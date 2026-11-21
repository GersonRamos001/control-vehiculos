# Etapa 1: Compilar el JAR de Spring Boot (Java 25 + Maven)
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Ejecutar la aplicación en runtime liviano (JRE 25)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Vercel asigna PORT en ejecución; 8080 se usa como valor por defecto
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -Dspring.docker.compose.enabled=false -jar app.jar"]
