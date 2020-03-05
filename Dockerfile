FROM gradle:jdk8 AS builder
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN  ./gradlew clean build -x test -Ppersist_git_info=true --stacktrace


FROM openjdk:8-alpine
COPY --from=builder /app/build/libs/*.war /app.war
EXPOSE 8080
EXPOSE 6006

# For local docker usage memory limit
ENV JAVA_OPTIONS -server -Xmx1024m -Xms256m  -Duser.timezone=UTC

CMD java $JAVA_OPTIONS -Dspring.cloud.consul.host=http://$CONSUL_HOST -Dspring.cloud.consul.port=$CONSUL_PORT -jar /app.war
