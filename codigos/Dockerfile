# Estágio 1: Build (Compilação)
FROM eclipse-temurin:25-jdk-alpine AS builder

# Atualiza os pacotes do SO para eliminar vulnerabilidades de segurança (CVEs)
RUN apk upgrade --no-cache --available

WORKDIR /app
COPY src/ ./src/

# Cria a pasta out e encontra todos os .java, EXCLUINDO a pasta de testes
RUN mkdir out && \
    find src -name "*.java" ! -path "src/test/*" > sources.txt && \
    javac -d out @sources.txt

# Estágio 2: Execução (Produção)
FROM eclipse-temurin:25-jre-alpine

# Patch de segurança também na imagem final de produção
RUN apk upgrade --no-cache --available

WORKDIR /app
COPY --from=builder /app/out ./out
COPY dados/ ./dados/

CMD ["java", "-cp", "out", "Main"]