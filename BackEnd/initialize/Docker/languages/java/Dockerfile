# Use the official OpenJDK image as the base image
FROM openjdk:11-jdk

#RUN apt-get update && apt-get install -y findutils

# 设置工作目录
WORKDIR /workdir

# 设置环境变量，用于传递C文件和头文件的目录
ENV SRC_DIR=/src
ENV TIMEOUT=1

COPY compile_and_run.sh /workdir/

# 编写构建脚本
RUN chmod +x /workdir/compile_and_run.sh

# 设置容器启动时执行的命令
ENTRYPOINT ["/workdir/compile_and_run.sh"]