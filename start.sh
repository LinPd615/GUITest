#!/bin/bash

echo "启动 Xvfb 虚拟显示..."
Xvfb :1 -screen 0 1024x768x16 &
export DISPLAY=:1
sleep 1

echo "启动 Fluxbox 桌面环境..."
fluxbox &
sleep 1

echo "启动 VNC 服务器..."
x11vnc -display :1 -nopw -forever -quiet &
sleep 1

echo "启动 noVNC 服务 (6080)..."
websockify --web=/opt/novnc 6080 localhost:5900 &
sleep 1

echo "启动 Java GUI 程序..."
cd /app/out
java GUIArithmeticOperate.ArithmeticApplication

wait

