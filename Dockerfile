FROM openjdk:8-jre-alpine
COPY target/api-pay-1.0.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
