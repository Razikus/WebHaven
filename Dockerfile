FROM node:20 as frontbuilder
COPY WebHavenFrontend /tobuild
WORKDIR /tobuild
RUN yarn && yarn run build

FROM maven:3.9.9-amazoncorretto-21-alpine as backbuilder
RUN apk add --no-cache apache-ant
COPY . /build
COPY build.xml /build

RUN rm -rf /build/WebHavenResources/dist
COPY --from=frontbuilder /tobuild/dist /build/WebHavenResources/dist
WORKDIR build

COPY pom.xml .
COPY build.xml .


RUN mvn dependency:go-offline

RUN ant -f build.xml extlib-env
RUN ant -f build.xml extlib/jogl
RUN ant -f build.xml extlib/lwjgl-base
RUN ant -f build.xml extlib/lwjgl-gl
RUN ant -f build.xml extlib/steamworks
RUN ant -f build.xml res-jar
RUN mvn clean install


FROM amazoncorretto:21-alpine as runner
COPY --from=backbuilder /build/target/WebHaven-0.2-with-dependencies.jar /WebHaven-0.2.jar


ENV HOST=0.0.0.0
ENV PORT=7901
ENV AUTOLOGIN_USER=""
ENV AUTOLOGIN_PASSWORD=""
ENV AUTOLOGIN_CHAR=""
ENTRYPOINT ["java", "-jar", "/WebHaven-0.2.jar"]