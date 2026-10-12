@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================================
echo    JiuZu Forum  /  Jiu Zu Lun Tan  -  one click starter
echo ============================================================
echo.
set PY=
where python >nul 2>nul
if %errorlevel%==0 set PY=python
if "%PY%"=="" (
  where py >nul 2>nul
  if %errorlevel%==0 set PY=py
)
if "%PY%"=="" (
  echo [ERROR] Python not found.
  echo Please install Python 3, or run manually:
  echo     build.py all
  pause
  exit /b 1
)
echo [1/3] compiling and deploying ...
%PY% build.py all
echo.
echo [2/3] opening browser ...
start "" http://localhost:8080/jiuzu-forum
echo.
echo [3/3] done. If the page is blank, wait 10 seconds and refresh.
echo Admin account: admin / admin123
pause
