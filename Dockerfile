FROM eclipse-temurin:17-jdk-alpine
#WORKDIR /app
#
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle .
#COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]