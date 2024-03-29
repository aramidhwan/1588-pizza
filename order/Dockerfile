FROM openjdk:8-jdk-alpine
COPY target/*SNAPSHOT.jar app.jar
EXPOSE 8091
# 만약 alpine 버전을 베이스로 한 docker image를 빌드 중이라면,
# 아주 가벼운 친구이기 때문에 timezone 파일들 또한 존재하지 않는다.
# 그래서 alpine 버전에서 사용하는 package manager인 'apk'를 사용하여
# tzdata package 를 설치하고, 그 후에 링크 작업을 실시한다.
RUN apk add tzdata && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

############################ JENNIFER SETTING START #########################
#####################
###   수정 필요   ###
#####################
ARG CONF_FILE_NM=order   # 제니퍼 대쉬보드에 표시할 업무 이름 (각 MSA별로 다르게 지정)
ARG DOMAIN_ID=2000       # 각 MSA별로 다르게 지정 (store는 1000, billing은 2000... 등)        

#####################
###   Don't Touch ###
#####################
ARG J_SERVER_IP=10.0.2.15
ARG AGENT_VERSION=5.6.2
ARG JENNIFER_HOME=/opt
ARG AGENT_HOME=$JENNIFER_HOME/agent.java
COPY jennifer-agent-java-$AGENT_VERSION.zip /
RUN unzip -oq jennifer-agent-java-${AGENT_VERSION}.zip -d /$JENNIFER_HOME
RUN `sed -i "s/domain_id = 1000/domain_id = $DOMAIN_ID/" $AGENT_HOME/conf/jennifer.conf`
RUN `sed -i "s/192.168.0.1/$J_SERVER_IP/" $AGENT_HOME/conf/jennifer.conf`
RUN `sed -i "4a\\inst_name=$CONF_FILE_NM" $AGENT_HOME/conf/jennifer.conf`
RUN `sed -i "4a\\ignore_url_postfix=\/healthcheck1" $AGENT_HOME/conf/jennifer.conf`
RUN `sed -i "4a\\ignore_url_postfix=\/healthcheck2" $AGENT_HOME/conf/jennifer.conf`
#####################
############################ JENNIFER SETTING END #########################

ENTRYPOINT ["java","-Xmx400M","-javaagent:/opt/agent.java/jennifer.jar","-Djennifer.config=/opt/agent.java/conf/jennifer.conf","-Djava.net.preferIPv4Stack=true","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=default"]
