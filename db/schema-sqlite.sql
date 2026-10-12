-- ==========================================================
-- 九组论坛 · SQLite 建表脚本
-- 说明：程序首次启动会自动执行同名建表语句，此脚本供查阅 / 手动建库使用
-- ==========================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  username        VARCHAR(50)  NOT NULL UNIQUE,
  password        VARCHAR(100) NOT NULL,   -- MD5(密码 + 盐)
  nickname        VARCHAR(50),
  avatar_color    VARCHAR(20)  DEFAULT '#0088f5',
  role            INT          DEFAULT 0,  -- 0 普通用户，1 管理员
  banned          INT          DEFAULT 0,  -- 0 正常，1 已封禁
  created_time    BIGINT,
  last_login_time BIGINT
);

-- 板块表
CREATE TABLE IF NOT EXISTS t_board (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  name        VARCHAR(50) NOT NULL UNIQUE,
  icon        VARCHAR(20),
  description VARCHAR(200),
  sort_order  INT DEFAULT 0
);

-- 帖子表
CREATE TABLE IF NOT EXISTS t_post (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  board_id     INT  NOT NULL,
  title        VARCHAR(200) NOT NULL,
  content      TEXT,
  user_id      INT  NOT NULL,
  views        INT  DEFAULT 0,
  likes        INT  DEFAULT 0,
  pinned       INT  DEFAULT 0,   -- 0 普通，1 置顶
  created_time BIGINT,
  updated_time BIGINT
);

-- 回复表
CREATE TABLE IF NOT EXISTS t_reply (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  post_id      INT NOT NULL,
  user_id      INT NOT NULL,
  content      TEXT,
  created_time BIGINT
);

-- 点赞表（联合主键，防止同一用户重复点赞）
CREATE TABLE IF NOT EXISTS t_like (
  post_id      INT NOT NULL,
  user_id      INT NOT NULL,
  created_time BIGINT,
  PRIMARY KEY (post_id, user_id)
);

-- 初始数据：板块
INSERT OR IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('综合讨论', '💬', '随便聊聊，什么都能发', 5);
INSERT OR IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('技术问答', '🛠', '遇到 Bug？把问题描述清楚', 4);
INSERT OR IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('资源分享', '📦', '好用的工具、教程、资料', 3);
INSERT OR IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('闲聊灌水', '🌊', '摸鱼、吐槽、八卦', 2);
INSERT OR IGNORE INTO t_board(name, icon, description, sort_order) VALUES ('公告区',   '📢', '官方通知，仅管理员发布', 1);
