FROM openeuler/openeuler:22.03-lts AS build
MAINTAINER Guofeng He <heguofeng@openeuler.sh>

RUN \
    sed -i "s/gpgcheck=1/gpgcheck=0/g" /etc/yum.repos.d/openEuler.repo && \
    sed -i "s#http://repo.openeuler.org#https://mirrors.huaweicloud.com/openeuler#g" /etc/yum.repos.d/openEuler.repo && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' >/etc/timezone && \
    yum -y update && yum clean all && \
    yum -y install wget make gcc g++ && yum clean all && \
    wget https://nodejs.org/download/release/v14.21.3/node-v14.21.3.tar.gz && \
    tar -xf node-v14.21.3.tar.gz && rm -rf node-v14.21.3.tar.gz && \
    cd node-v14.21.3 && \
    ./configure && make -j8 && make install && \
    cd .. && rm -rf node-v14.21.3

COPY . /app
WORKDIR /app
RUN \ 
  npm run build:prod 

FROM openeuler/openeuler:22.03-lts
MAINTAINER Guofeng He <heguofeng@openeuler.sh>
RUN \
    sed -i "s#http://repo.openeuler.org#https://mirrors.huaweicloud.com/openeuler#g" /etc/yum.repos.d/openEuler.repo && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' >/etc/timezone && \
    yum -y install nginx && \
    yum clean all

COPY --from=build /app/dist  /usr/share/nginx/html
COPY nginx.conf /etc/nginx/

ENTRYPOINT ["nginx", "-g", "daemon off;"]

