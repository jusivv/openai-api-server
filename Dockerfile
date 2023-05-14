FROM openjdk:17.0.2-jdk
ARG PROJECT_VERSION=1.0.0
WORKDIR /app
COPY ./target/openai-api-server-${PROJECT_VERSION}.jar ./openai-api-server-${PROJECT_VERSION}.jar
RUN echo "java -jar /app/openai-api-server-${PROJECT_VERSION}.jar --server.port=8080" > startup.sh && chmod 755 startup.sh
CMD /app/startup.sh
