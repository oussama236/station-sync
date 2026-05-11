FROM eclipse-temurin:17-jre

WORKDIR /app

RUN useradd -m appuser

COPY app.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java","-jar","/app/app.jar"]