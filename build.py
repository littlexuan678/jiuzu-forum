# -*- coding: utf-8 -*-
"""
九组论坛 - 构建部署脚本

用法（需 Python 3.7+）：
    python build.py compile   编译 Java 源码到 WEB-INF/classes
    python build.py war       打包成 jiuzu-forum.war
    python build.py deploy    部署到本机 Tomcat（自动复制到 webapps）
    python build.py start     启动 Tomcat
    python build.py stop      停止 Tomcat
    python build.py all       编译 + 部署 + 启动（日常一键跑起来）

说明：JDK 优先使用脚本自动探测到的路径；若本机 JDK 位置不同，
      修改下方 CANDIDATE_JDKS 或直接设置环境变量 JAVA_HOME。
"""
import glob
import os
import shutil
import subprocess
import sys
import time

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "src")
WEB = os.path.join(BASE, "WebContent")
CLASSES = os.path.join(WEB, "WEB-INF", "classes")
LIB = os.path.join(WEB, "WEB-INF", "lib")

# 候选 JDK 路径（按顺序探测）
CANDIDATE_JDKS = [
    r"D:\Java\zulu8.80.0.17-ca-jdk8.0.422-win_x64",
    r"C:\Java\jdk1.8.0_202",
    r"C:\Program Files\Java\jdk1.8.0_202",
]
# 候选 Tomcat 路径
CANDIDATE_TOMCATS = [
    r"C:\Java\apache-tomcat-8.5.100",
    r"D:\Java\apache-tomcat-8.5.100",
    r"C:\apache-tomcat-8.5.100",
]

APP_NAME = "jiuzu-forum"


def find_first(paths, marker="bin"):
    """从候选路径里挑一个真实存在且带 bin 目录的"""
    for p in paths:
        if os.path.isdir(p) and os.path.isdir(os.path.join(p, marker)):
            return p
    for p in paths:
        if os.path.isdir(p):
            return p
    env_home = os.environ.get("JAVA_HOME") or os.environ.get("CATALINA_HOME")
    if env_home and os.path.isdir(env_home):
        return env_home
    return None


def get_jdk():
    jdk = os.environ.get("BUILD_JAVA_HOME")
    if jdk and os.path.isdir(jdk):
        return jdk
    return find_first(CANDIDATE_JDKS)


def get_tomcat():
    tomcat = os.environ.get("BUILD_TOMCAT_HOME")
    if tomcat and os.path.isdir(tomcat):
        return tomcat
    return find_first(CANDIDATE_TOMCATS)


def build_classpath(jdk, tomcat):
    """编译需要的类路径：Tomcat 的 servlet-api + 项目依赖 jar"""
    jars = []
    if tomcat:
        api = os.path.join(tomcat, "lib", "servlet-api.jar")
        if os.path.exists(api):
            jars.append(api)
    jars += glob.glob(os.path.join(LIB, "*.jar"))
    return ";".join(jars)


def run(cmd, cwd=None, wait=True, env=None):
    merged = os.environ.copy()
    if env:
        merged.update(env)
    if wait:
        r = subprocess.run(cmd, cwd=cwd, env=merged,
                           stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        out = r.stdout.decode("utf-8", "replace")
        return r.returncode, out
    subprocess.Popen(cmd, cwd=cwd, env=merged,
                     stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return 0, ""


def compile_java():
    jdk = get_jdk()
    tomcat = get_tomcat()
    if not jdk or not os.path.exists(os.path.join(jdk, "bin", "javac.exe")):
        print("[错误] 找不到可用的 JDK，请修改 CANDIDATE_JDKS 或设置 BUILD_JAVA_HOME")
        return False
    files = glob.glob(os.path.join(SRC, "**", "*.java"), recursive=True)
    if not files:
        print("[错误] src 下没有 Java 文件")
        return False
    os.makedirs(CLASSES, exist_ok=True)
    javac = os.path.join(jdk, "bin", "javac.exe")
    cmd = [javac, "-encoding", "UTF-8", "-nowarn",
           "-d", CLASSES, "-cp", build_classpath(jdk, tomcat)] + files
    print("[1/3] 编译 %d 个源文件 …" % len(files))
    code, out = run(cmd)
    if code != 0:
        print("[失败] 编译出错：")
        print(out[:4000])
        return False
    print("[完成] 编译成功，class 输出至 WEB-INF/classes")
    return True


def deploy():
    tomcat = get_tomcat()
    if not tomcat:
        print("[错误] 找不到 Tomcat，请修改 CANDIDATE_TOMCATS 或设置 BUILD_TOMCAT_HOME")
        return False
    target = os.path.join(tomcat, "webapps", APP_NAME)
    print("[2/3] 部署到 %s …" % target)
    if os.path.isdir(target):
        # 保留线上数据目录，避免每次部署都清空论坛内容
        data_src = os.path.join(target, "WEB-INF", "data")
        if os.path.isdir(data_src):
            backup = target + "_data_backup"
            if os.path.isdir(backup):
                shutil.rmtree(backup, ignore_errors=True)
            shutil.move(data_src, backup)
            shutil.rmtree(target, ignore_errors=True)
            os.makedirs(os.path.dirname(data_src), exist_ok=True)
            shutil.move(backup, data_src)
        else:
            shutil.rmtree(target, ignore_errors=True)
    shutil.copytree(WEB, target, dirs_exist_ok=True)
    print("[完成] 已部署：%s" % target)
    return True


def start_tomcat():
    tomcat = get_tomcat()
    jdk = get_jdk()
    if not tomcat:
        print("[错误] 找不到 Tomcat")
        return False
    print("[3/3] 启动 Tomcat …")
    bat = os.path.join(tomcat, "bin", "startup.bat")
    env = {"JAVA_HOME": jdk} if jdk else {}
    if os.name == "nt":
        run(["cmd.exe", "/c", bat], cwd=os.path.join(tomcat, "bin"), wait=False, env=env)
    else:
        run([os.path.join(tomcat, "bin", "startup.sh")], wait=False, env=env)
    for _ in range(30):
        time.sleep(1)
        code, out = run(["cmd.exe", "/c", "echo.|set /p= >nul"], wait=True) if False else (0, "")
        probe = probe_server()
        if probe:
            print("[完成] 服务已就绪：%s" % probe)
            return True
    print("[提示] Tomcat 正在启动，请稍后用浏览器访问 http://localhost:8080/%s" % APP_NAME)
    return True


def probe_server():
    import urllib.request
    url = "http://localhost:8080/%s/posts" % APP_NAME
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "build-script"})
        with urllib.request.urlopen(req, timeout=3) as resp:
            if resp.status == 200:
                return url
    except Exception:
        return None
    return None


def stop_tomcat():
    tomcat = get_tomcat()
    if not tomcat:
        return False
    bat = os.path.join(tomcat, "bin", "shutdown.bat")
    jdk = get_jdk()
    env = {"JAVA_HOME": jdk} if jdk else {}
    if os.name == "nt":
        run(["cmd.exe", "/c", bat], cwd=os.path.join(tomcat, "bin"), wait=True, env=env)
    else:
        run([os.path.join(tomcat, "bin", "shutdown.sh")], wait=True, env=env)
    print("[完成] 已发送停止命令")
    return True


def make_war():
    jdk = get_jdk()
    if not jdk:
        print("[错误] 找不到 JDK")
        return False
    war = os.path.join(BASE, APP_NAME + ".war")
    jar = os.path.join(jdk, "bin", "jar.exe")
    if os.path.exists(war):
        os.remove(war)
    print("[打包] 生成 %s" % war)
    code, out = run([jar, "cvf", war, "-C", WEB, "."], wait=True)
    if code != 0:
        print(out[:2000])
        return False
    print("[完成] 大小 %.1f MB" % (os.path.getsize(war) / 1024 / 1024))
    return True


def main():
    cmd = sys.argv[1] if len(sys.argv) > 1 else "all"
    print("=" * 60)
    print("九组论坛 构建工具   JDK=%s" % get_jdk())
    print("                   Tomcat=%s" % get_tomcat())
    print("=" * 60)
    if cmd == "compile":
        compile_java()
    elif cmd == "deploy":
        deploy()
    elif cmd == "start":
        start_tomcat()
    elif cmd == "stop":
        stop_tomcat()
    elif cmd == "war":
        compile_java() and make_war()
    elif cmd == "all":
        if compile_java() and deploy():
            start_tomcat()
    else:
        print(__doc__)


if __name__ == "__main__":
    main()
