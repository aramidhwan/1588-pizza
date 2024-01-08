FROM openjdk:8-jdk-alpine
COPY target/*SNAPSHOT.jar app.jar
EXPOSE 8080

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
ARG AGENT_VERSION=5.5.3
ARG JENNIFER_HOME=/opt
ARG AGENT_HOME=$JENNIFER_HOME/agent.java
COPY jennifer-agent-java-$AGENT_VERSION.zip /
RUN unzip -oq jennifer-agent-java-${AGENT_VERSION}.zip -d /$JENNIFER_HOME
RUN value=`cat $AGENT_HOME/conf/jennifer.conf` && echo "${value//domain_id = 1000/domain_id = $DOMAIN_ID}" >| $AGENT_HOME/conf/$CONF_FILE_NM.conf
RUN value=`cat $AGENT_HOME/conf/$CONF_FILE_NM.conf` && echo "${value//192.168.0.1/$J_SERVER_IP}" >| $AGENT_HOME/conf/$CONF_FILE_NM.conf
#####################
############################ JENNIFER SETTING END #########################

ENTRYPOINT ["java","-Xmx400M","-javaagent:/opt/agent.java/jennifer.jar","-Djennifer.config=/opt/agent.java/conf/order.conf","-Djava.net.preferIPv4Stack=true","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=default"]
