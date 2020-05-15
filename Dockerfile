# docker build --compress --force-rm --rm --no-cache -t hackorama/plethora .
# docker run --name plethora -d -p 9999:9999 hackorama/plethora
FROM openjdk:8-jre-alpine

RUN apk add bash

WORKDIR /plethora

COPY lib lib
COPY build build
COPY src/main/resources/web web
ADD docker.plethora.properties plethora.properties
ADD entrypoint.sh entrypoint.sh

RUN chmod a+x entrypoint.sh

EXPOSE 9999 9999

ENTRYPOINT [ "/plethora/entrypoint.sh" ]
