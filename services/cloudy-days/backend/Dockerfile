FROM openjdk:14-jdk-alpine as build
WORKDIR /app
COPY gradle gradle
COPY build.gradle gradlew ./
COPY src src
RUN ./gradlew bootJar

FROM openjdk:14-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/app-0.0.1-SNAPSHOT.jar /app/cloudydays.jar
ENTRYPOINT ["java","-jar", "/app/cloudydays.jar"]