FROM openjdk:8-jre-alpine
RUN apk --no-cache add ca-certificates
WORKDIR /running/
COPY deploy/run.sh run.sh
RUN chmod +x run.sh
COPY build/libs/app.jar .
EXPOSE 7001
ENTRYPOINT ./run.sh java -Djava.security.egd=file:/dev/./urandom -Dsun.net.inetaddr.ttl=60 -jar app.jar