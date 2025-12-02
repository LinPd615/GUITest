# GUITest
GUITest – Java Swing GUI + Docker + noVNC
一个用于运行 Java Swing 图形界面的可视化应用，并支持在服务器上通过浏览器远程访问（通过 x11vnc + noVNC 提供 Web 桌面界面）。
本项目支持：
Java Swing GUI 可视化界面
Docker 一键封装
服务器无桌面环境运行 GUI
通过浏览器访问（http://服务器IP:6080）
UTF-8 编码全支持（修复中文乱码）
自动构建（GitHub Actions，可选）

GUITest/
│── Dockerfile
│── start.sh
│── README.md
│── .gitignore
│
├── src/
│     ├── Main.java
│     ├── GUIArithmeticOperate/
│     └── META-INF/
│
└── assets/（可选）

功能简介
本项目适用于：
Java GUI 界面开发
教学 / 演示 / 练习题生成工具
服务器无桌面环境（SSH-only）但需要显示 GUI 的场景
远程访问 GUI 应用（浏览器即可）
无需在服务器安装桌面，只需运行容器即可。

构建 Docker 镜像
执行：
docker build -t guitest-gui .
成功后运行：
docker run -d \
    --name guitest-gui \
    -p 6080:6080 \
    guitest-gui

访问 Java GUI
浏览器打开：http://服务器IP:6080
你将看到：
Fluxbox 桌面环境
自动运行的 Java GUI 程序

修改代码后更新镜像
每次修改 Java 代码后：
git add .
git commit -m "update"
git push

服务器更新代码：
git pull
docker build -t guitest-gui .
docker restart guitest-gui

常见问题
1. 中文乱码？
原因：Java 默认编码为 ASCII 或 latin1
解决：编译时强制 UTF-8
Dockerfile 已包含：
javac -encoding UTF-8 ...

Auto Build (GitHub Actions)
.github/workflows/docker-build.yml

Rollback
GitHub 回滚：
git log --oneline
git reset --hard <commit-id>
git push --force

Docker 回滚：
docker pull your/repo:previous
docker run your/repo:previous

