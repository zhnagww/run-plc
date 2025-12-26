@echo off
chcp 65001 >nul 2>&1
title Run PLC Application
color 0A

REM 获取当前目录
set "APP_DIR=%~dp0"
echo "%APP_DIR%"
cd /d "%APP_DIR%"

REM 检查JRE是否存在
if not exist "jre\bin\java.exe" (
    echo 错误：未找到Java运行时环境（JRE）。
    echo 请将JRE 8放置在当前目录的jre文件夹中。
    echo 或者，您可以自行安装Java 8，并确保java命令在PATH中。
    pause
    exit /b 1
)

REM 设置JAVA_HOME并使用我们的JRE
set "JAVA_HOME=%APP_DIR%jre"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM 启动应用程序
echo 正在启动Run PLC应用程序...
jre\bin\java.exe -Dfile.encoding=UTF-8 -jar run-plc-1.0.jar

REM 如果上面的命令失败，尝试使用系统Java
if errorlevel 1 (
    echo 使用内置JRE启动失败，尝试使用系统Java...
    java -jar run-plc-1.0.jar
    if errorlevel 1 (
        echo 启动失败，请确保已安装Java 8或更高版本。
        pause
    )
)