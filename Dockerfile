FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -m appuser
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]