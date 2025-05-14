FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . /app

RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/Station-Sync-0.0.1-SNAPSHOT.jar"]