FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080 1097

ENV JAVA_OPTS="\
  -Dcom.sun.management.jmxremote.port=1097 \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Djava.rmi.server.hostname=localhost \
  -Djava.net.preferIPv4Stack=true \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/ \
  -XX:+ExitOnOutOfMemoryError \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
