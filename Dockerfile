FROM openjdk:8-jdk-alpine
ARG APP_VERSION=1.0.0
ENV APP_HOME /app
EXPOSE 8899
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME
COPY target/elasticsearch-demo-0.0.1-SNAPSHOT.jar $APP_HOME/es-demo.jar
CMD ["java","-Xmx512m","-Xms512m", "-jar --spring.profiles.active=prod", "es-demo.jar"]

