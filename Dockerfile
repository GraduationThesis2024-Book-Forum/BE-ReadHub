FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app
COPY --from=build /app/build/libs/BE-Readhub-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]