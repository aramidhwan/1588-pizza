FROM openjdk:8u212-jdk-alpine
COPY target/*SNAPSHOT.jar app.jar
EXPOSE 8090
# 만약 alpine 버전을 베이스로 한 docker image를 빌드 중이라면,
# 아주 가벼운 친구이기 때문에 timezone 파일들 또한 존재하지 않는다.
# 그래서 alpine 버전에서 사용하는 package manager인 'apk'를 사용하여
# tzdata package 를 설치하고, 그 후에 링크 작업을 실시한다.
RUN apk add tzdata && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=default"]
