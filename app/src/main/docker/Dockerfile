FROM openjdk:8-jdk

ENV JAVA_OPTS=""

RUN mkdir -p /opt/deploy
ADD run-app.sh /opt/deploy/run-app.sh
RUN chmod u+x /opt/deploy/run-app.sh
ADD ${project.name}-${project.version}-boot.jar /opt/deploy/${project.name}-${project.version}-boot.jar

CMD ["/opt/deploy/run-app.sh", "/opt/deploy/${project.name}-${project.version}-boot.jar"]
