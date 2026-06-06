FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package
RUN java -Djarmode=layertools -jar target/Product-Service-0.0.1-SNAPSHOT.jar extract


FROM eclipse-temurin:17-jdk-jammy AS jre-build

RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.crypto.ec,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /opt/java-minimal


FROM gcr.io/distroless/base-debian12:nonroot

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

COPY --from=jre-build /opt/java-minimal /opt/java-minimal
COPY --from=build /workspace/dependencies/ ./
COPY --from=build /workspace/snapshot-dependencies/ ./
COPY --from=build /workspace/spring-boot-loader/ ./
COPY --from=build /workspace/application/ ./

EXPOSE 8083

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

