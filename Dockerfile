FROM eclipse-temurin:17_35-jdk-alpine
LABEL maintainer="go"

WORKDIR /app

RUN apk add --no-cache fontconfig ttf-dejavu

COPY target/go-1.0.0-fat.jar app.jar

EXPOSE 8899

ENTRYPOINT ["java", "-jar", "app.jar", "-conf", "config.json"]
