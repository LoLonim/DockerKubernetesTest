FROM openjdk:17-oracle
ARG JAR_FILE=target/testapp-0.0.1-SNAPSHOT.jar
ARG USER=app
ARG UID=1001
ARG GID=1001
RUN useradd ${USER} \
   && usermod -u $UID ${USER} \
   && groupmod -g $GID ${USER} \
   && mkdir -p /app \
   && chown -R ${USER}:${USER} /app
USER ${USER}
COPY --chown=$USER:$USER hello.html /app
WORKDIR /app
EXPOSE 8000
COPY --chown=$USER:$USER ${JAR_FILE} app.jar  
CMD ["java", "-jar", "app.jar"] 
