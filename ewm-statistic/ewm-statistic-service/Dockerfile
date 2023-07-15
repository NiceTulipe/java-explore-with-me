FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar statistic-service.jar
ENTRYPOINT ["java","-jar","/statistic-service.jar"]