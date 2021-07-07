FROM maven:latest as builder
WORKDIR /app
COPY ./src/ ./src/
COPY ./pom.xml ./pom.xml
RUN mvn clean install

FROM adoptopenjdk/openjdk12
WORKDIR /app
COPY --from=builder /app/target/dependency-jars ./dependency-jars
COPY --from=builder /app/target/sfsc-cp-datapoint-1.0-SNAPSHOT.jar .
COPY --from=builder /app/target/classes/logback.xml .

ENV EXTERNEL_CORE_IP 127.0.0.1
ENV EXTERNEL_CORE_PORT 1251

CMD ["java", "-jar", "sfsc-cp-datapoint-1.0-SNAPSHOT.jar"]
