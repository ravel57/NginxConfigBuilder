FROM node:20-alpine3.19 AS nodejs
RUN apk add --no-cache git
WORKDIR /home/node
RUN git clone https://github.com/ravel57/nginx-config-builder.git
WORKDIR /home/node/nginx-config-builder
RUN yarn install
RUN yarn build

FROM gradle:8.7.0-jdk21-alpine AS gradle
COPY --chown=gradle:gradle . /home/gradle/
COPY --from=nodejs /home/node/nginx-config-builder/dist/spa/.  /home/gradle/src/main/resources/static/
WORKDIR  /home/gradle/
RUN gradle bootJar

FROM alpine/java:22-jdk AS java
WORKDIR /home/java/
COPY --from=gradle /home/gradle/build/libs/*.jar /home/java/NginxConfigBuilder.jar
RUN apk add --no-cache certbot nginx openrc
CMD ["java", "-jar", "NginxConfigBuilder.jar"]