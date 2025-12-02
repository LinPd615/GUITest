FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# 安装 Java 和图形环境
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    xvfb \
    fluxbox \
    wget \
    net-tools \
    supervisor \
    x11vnc \
    python3-websockify \
    fonts-dejavu \
    fonts-wqy-zenhei \
    fonts-wqy-microhei \
    && rm -rf /var/lib/apt/lists/*

# 安装 noVNC
RUN mkdir -p /opt/novnc && \
    wget -qO- https://github.com/novnc/noVNC/archive/refs/heads/master.tar.gz \
    | tar xz --strip-components 1 -C /opt/novnc && \
    ln -s /opt/novnc/vnc_lite.html /opt/novnc/index.html

# 创建 exercise 目录（数据持久）
RUN mkdir -p /root/exercise

# 拷贝 Java 源码
COPY src/ /app/src/

# 编译 Java 源码
RUN mkdir -p /app/out && \
    javac -encoding UTF-8 $(find /app/src -name "*.java") -d /app/out

# 拷贝启动脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

WORKDIR /app/out

EXPOSE 6080

CMD ["/start.sh"]

