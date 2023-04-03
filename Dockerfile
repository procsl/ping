# 该镜像需要依赖的基础镜像
FROM openjdk:17
# 将当前目录下的jar包复制到docker容器的/目录下

ADD ./ping-distribute/target/ping-distribute-1.0.0.jar ~/ping-distribute.jar

# 运行过程中创建一个mall-tiny-docker-file.jar文件
ENV DB_USERNAME root
ENV DB_PASSWORD 123456
ENV spring.profiles.dev=monitor,dev

# 声明服务运行在8080端口
EXPOSE 10000

# 指定docker容器启动时运行jar包
ENTRYPOINT ["java", "-jar","~/ping-distribute.jar"]

# 指定维护者的名字
MAINTAINER procsl