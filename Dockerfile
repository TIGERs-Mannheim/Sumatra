FROM maven:3.5.2-jdk-8 as builder
WORKDIR /Sumatra
RUN chown 1000 /Sumatra
USER 1000
COPY --chown=1000 pom.xml build.sh ./
COPY --chown=1000 modules modules/
COPY --chown=1000 .git .git/
RUN ./build.sh

FROM openjdk:8u181-jdk-stretch
WORKDIR /Sumatra
RUN chown 1000 /Sumatra
USER 1000
RUN mkdir -p modules/sumatra-main/target
ADD --chown=1000 run.sh ./
COPY --chown=1000 --from=builder /Sumatra/modules/sumatra-main/target/lib modules/sumatra-main/target/lib
COPY --chown=1000 --from=builder /Sumatra/modules/moduli-referee/target/ssl-game-controller modules/moduli-referee/target/ssl-game-controller

ENTRYPOINT ["./run.sh", "-hl", "-s"]
CMD [""]