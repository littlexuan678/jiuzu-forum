@echo off
chcp 65001 >nul
cd /d "%~dp0"
python build.py stop
echo Tomcat stop command sent.
pause
