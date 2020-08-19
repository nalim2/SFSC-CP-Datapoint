FROM --platform=linux/arm/v7 adoptopenjdk/openjdk12
# --platform=linux/arm/v7
WORKDIR /app

COPY /target/dependency-jars /dependency-jars
COPY target/sfsc-cp-datapoint-0.1.0-SNAPSHOT.jar .
COPY target/classes/logback.xml .
ENV EXTERNEL_CORE_IP 127.0.0.1
ENV EXTERNEL_CORE_PORT 1251

CMD ["java", "-jar", "sfsc-cp-datapoint-0.1.0-SNAPSHOT.jar"]
