FROM node:20 as frontbuilder
COPY src/main/resources/public/WebHaven /tobuild
WORKDIR /tobuild
RUN yarn && yarn run build

FROM maven:3.9.9-amazoncorretto-21-alpine as backbuilder

COPY . /build
RUN rm -rf /build/src/main/resources/public/dist
COPY --from=frontbuilder /tobuild/dist /build/src/main/resources/public/dist
WORKDIR build
RUN mvn clean install


FROM amazoncorretto:21-alpine as runner
COPY --from=backbuilder /build/target/WebHaven-0.1.jar /WebHaven-0.1.jar


ENV HOST=0.0.0.0
ENV PORT=7901
ENV AUTOLOGIN_USER=
ENV AUTOLOGIN_PASSWORD=
ENV AUTOLOGIN_CHAR=
ENTRYPOINT ["java", "-jar", "/WebHaven-0.1.jar"]