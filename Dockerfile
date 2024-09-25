FROM gradle:8.7.0-jdk21-alpine AS gradle

COPY --chown=gradle:gradle . /home/gradle/
WORKDIR  /home/gradle/
RUN apk add --no-cache certbot
RUN mkdir -p /var/www/html
RUN gradle bootJar
CMD ["java", "-jar", "build/libs/NginxConfigBuilder-0.1.jar"]

#FROM alpine/java:22-jdk AS java