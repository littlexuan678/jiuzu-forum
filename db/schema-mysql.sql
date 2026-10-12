-- ==========================================================
-- 九组论坛 · MySQL 建表脚本（可选方案）
-- 用法：先建库 CREATE DATABASE jiuzu_forum DEFAULT CHARSET utf8mb4;
--       再执行本脚本，并把 db.properties 复制为 src/db.properties
-- ==========================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
  id              INT PRIMARY KEY AUTO_INCREMENT,
  username        VARCHAR(50)  NOT NULL UNIQUE,
  password        VARCHAR(100) NOT NULL,
  nickname        VARCHAR(50),
  avatar_color    VARCHAR(20)  DEFAULT '#0088f5',
  role            INT          DEFAULT 0,
  banned          INT          DEFAULT 0,
  created_time    BIGINT,
  last_login_time BIGINT
) DEFAULT CHARSET = utf8mb4;

-- 板块表
CREATE TABLE IF NOT EXISTS t_board (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(50) NOT NULL UNIQUE,
  icon        VARCHAR(20),
  description VARCHAR(200),
  sort_order  INT DEFAULT 0
) DEFAULT CHARSET = utf8mb4;

-- 帖子表
CREATE TABLE IF NOT EXISTS t_post (
  id           INT PRIMARY KEY AUTO_INCREMENT,
  board_id     INT  NOT NULL,
  title        VARCHAR(200) NOT NULL,
  content      TEXT,
  user_id      INT  NOT NULL,
  views        INT  DEFAULT 0,
  likes        INT  DEFAULT 0,
  pinned       INT  DEFAULT 0,
  created_time BIGINT,
  updated_time BIGINT
) DEFAULT CHARSET = utf8mb4;

-- 回复表
CREATE TABLE IF NOT EXISTS t_reply (
  id           INT PRIMARY KEY AUTO_INCREMENT,
  post_id      INT NOT NULL,
  user_id      INT NOT NULL,
  content      TEXT,
  created_time BIGINT
) DEFAULT CHARSET = utf8mb4;

-- 点赞表
CREATE TABLE IF NOT EXISTS t_like (
  post_id      INT NOT NULL,
  user_id      INT NOT NULL,
  created_time BIGINT,
  PRIMARY KEY (post_id, user_id)
) DEFAULT CHARSET = utf8mb4;

-- 初始板块数据
INSERT IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('综合讨论', '💬', '随便聊聊，什么都能发', 5);
INSERT IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('技术问答', '🛠', '遇到 Bug？把问题描述清楚', 4);
INSERT IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('资源分享', '📦', '好用的工具、教程、资料', 3);
INSERT IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('闲聊灌水', '🌊', '摸鱼、吐槽、八卦', 2);
INSERT IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('公告区',   '📢', '官方通知，仅管理员发布', 1);

-- 管理员账号（密码 MD5 加盐后为：admin123）
-- 建议使用项目首次启动时自动生成，此处仅作参考
-- INSERT INTO t_user(username, password, nickname, avatar_color, role, banned, created_time)
-- VALUES ('admin', '<由程序生成>', '管理员', '#f56a79', 1, 0, UNIX_TIMESTAMP() * 1000);
