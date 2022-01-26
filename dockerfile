FROM openjdk:8-jre-alpine
COPY ./target/utopia.users-0.0.1-SNAPSHOT.jar /usr/src/utopia.jar
WORKDIR /usr/src/

CMD ["java", "-jar", "utopia.jar"]