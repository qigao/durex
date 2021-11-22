FROM adoptopenjdk/openjdk11:latest
EXPOSE 8080
RUN mkdir /opt/app
COPY app.jar /opt/app
CMD ["java", "-jar", "/opt/app/app.jar"]
