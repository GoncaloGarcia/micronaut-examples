FROM java:openjdk-8u111-alpine
RUN apk --no-cache add curl
CMD ./wait-for -t 60 $CONSUL_HOST:$CONSUL_PORT -- \
    echo "All dependencies ready. Starting application..." && \
    java ${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044 -jar mail-all.jar
COPY build/libs/*-all.jar mail-all.jar
COPY wait-for wait-for