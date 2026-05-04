FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY . .
RUN cd taskmanager && mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "taskmanager/target/taskmanager-0.0.1-SNAPSHOT.jar"]
