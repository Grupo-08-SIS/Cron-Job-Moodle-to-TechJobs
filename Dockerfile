# Use a imagem base do Maven para compilar o projeto
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Defina o diretório de trabalho no contêiner
WORKDIR /app

# Copie o arquivo pom.xml e as dependências do Maven
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
COPY mvnw.cmd ./

# Converta o script mvnw para Unix e garanta permissão de execução
RUN apt-get update && apt-get install -y dos2unix && dos2unix mvnw && chmod +x mvnw

# Baixe as dependências do Maven
RUN ./mvnw dependency:go-offline

# Copie o restante do código-fonte do projeto
COPY src ./src

# Compile o projeto
RUN ./mvnw package -DskipTests

# Use uma imagem base do OpenJDK para rodar o aplicativo
FROM eclipse-temurin:17-jdk-jammy

# Defina o diretório de trabalho no contêiner
WORKDIR /app

# Copie o arquivo JAR do estágio de build
COPY --from=build /app/target/TechCronJobs-0.0.1-SNAPSHOT.jar app.jar

# Exponha a porta que a aplicação irá rodar
EXPOSE 8081

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
