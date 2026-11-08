# Compila o WAR com Java 11; o Maven resolve as dependencias do pom.xml.
FROM maven:3.10.0-eclipse-temurin-11 AS build
WORKDIR /app
COPY backend/demo/pom.xml ./pom.xml
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY backend/demo/src ./src
RUN mvn --batch-mode --no-transfer-progress verify

# Tomcat 10.1 suporta jakarta.servlet e precisa de Java 11 ou superior.
# O bytecode Java 11 do projeto pode ser executado no Java 17 desta imagem.
FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/demo-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
# Render encaminha o trafego para a porta definida em PORT.
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT}\\\"/\" /usr/local/tomcat/conf/server.xml && exec catalina.sh run"]
