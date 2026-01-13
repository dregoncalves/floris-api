# Estágio de Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos do Maven Wrapper e o pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dá permissão de execução ao mvnw e baixa as dependências (cache layer)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copia o código fonte e faz o build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Estágio de Execução (Imagem final mais leve)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta definida no application.properties (padrao 8080)
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]