FROM openeuler/openeuler:22.03-lts AS build
MAINTAINER Guofeng He <heguofeng@openeuler.sh>

RUN \
    sed -i "s/gpgcheck=1/gpgcheck=0/g" /etc/yum.repos.d/openEuler.repo && \
    sed -i "s#http://repo.openeuler.org#https://mirrors.huaweicloud.com/openeuler#g" /etc/yum.repos.d/openEuler.repo  && \
    yum -y update && \
    yum install -y java util-linux maven && \
    sed -i "s/TMOUT=300/TMOUT=0/g" /etc/bashrc && \
    yum clean all
COPY . /app
WORKDIR /app
RUN mvn package 

FROM openeuler/openeuler:22.03-lts 
MAINTAINER Guofeng He <heguofeng@openeuler.sh>

RUN \
    sed -i "s/gpgcheck=1/gpgcheck=0/g" /etc/yum.repos.d/openEuler.repo && \
    sed -i "s#http://repo.openeuler.org#https://mirrors.huaweicloud.com/openeuler#g" /etc/yum.repos.d/openEuler.repo  && \
    yum install -y java && \
    sed -i "s/TMOUT=300/TMOUT=0/g" /etc/bashrc && \
    yum clean all

COPY --from=build /app/target/opengauss-batman.jar /app/opengauss-batman.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/opengauss-batman.jar"]

