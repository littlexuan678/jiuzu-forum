package com.jiuzu.forum.service;

import com.jiuzu.forum.bean.Board;
import com.jiuzu.forum.dao.BoardDao;

import java.util.List;

/**
 * 板块业务逻辑层
 *
 * @author 第九小组
 */
public class BoardService {

    private BoardDao boardDao = new BoardDao();

    /** 查询全部板块（含帖子数） */
    public List<Board> listAll() {
        return boardDao.findAllWithCount();
    }

    /** 根据 ID 查询板块 */
    public Board getById(int id) {
        return boardDao.findById(id);
    }

    /** 是否为公告区（仅管理员可发帖） */
    public boolean isAnnouncementBoard(int boardId) {
        Board b = boardDao.findById(boardId);
        return b != null && "公告区".equals(b.getName());
    }

    /** 新增板块 */
    public void create(String name, String icon, String description, int sortOrder) {
        Board b = new Board(name, icon, description, sortOrder);
        boardDao.insert(b);
    }

    /** 删除板块 */
    public void delete(int id) {
        boardDao.delete(id);
    }
}
