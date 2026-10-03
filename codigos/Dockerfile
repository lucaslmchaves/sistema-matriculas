# Estágio 1: Build (Compilação)
FROM eclipse-temurin:21-jdk-alpine AS builder

# Atualiza os pacotes do SO para eliminar vulnerabilidades de segurança (CVEs)
RUN apk upgrade --no-cache --available

WORKDIR /app
COPY src/ ./src/

RUN mkdir out && \
    find src -name "*.java" > sources.txt && \
    javac -d out @sources.txt

# Estágio 2: Execução (Produção)
FROM eclipse-temurin:21-jre-alpine

# Patch de segurança também na imagem final de produção
RUN apk upgrade --no-cache --available

WORKDIR /app
COPY --from=builder /app/out ./out
COPY dados/ ./dados/

CMD ["java", "-cp", "out", "matriculas.Main"]