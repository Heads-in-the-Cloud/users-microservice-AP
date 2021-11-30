FROM alpine
COPY ./target/utopia.users-0.0.1-SNAPSHOT.jar /usr/src/utopia.jar
WORKDIR /usr/src/
RUN apk -U add openjdk13
CMD ["java", "-jar", "utopia.jar"]