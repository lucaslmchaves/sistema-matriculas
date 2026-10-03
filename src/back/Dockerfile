FROM eclipse-temurin:25-jdk AS compilacao
WORKDIR /build
COPY matriculas ./matriculas
RUN javac -encoding UTF-8 -d out $(find matriculas -name "*.java")

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=compilacao /build/out ./out
RUN mkdir dados && chown -R 1000:1000 /app
USER 1000:1000

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-cp", "out", "matriculas.MainWeb"]
