FROM gradle:jdk8-alpine as builder
ADD --chown=gradle:gradle . /home/gradle/reinmock
WORKDIR /home/gradle/reinmock

RUN gradle build


FROM openjdk:8-jre-alpine

ENV APPNAME=reinmock PORT=10001
WORKDIR /app

RUN adduser -u $PORT -H -D $APPNAME && \
    chown -R $APPNAME .

COPY --from=builder /home/gradle/reinmock/build/libs/reinmock.jar /app/reinmock.jar

USER $APPNAME

CMD ["java", "-server", "-Xms4g", "-XX:OnOutOfMemoryError='kill -9 %p'", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "reinmock.jar"]
