package com.jiuzu.forum.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 分页封装类（JavaBean）
 *
 * <p>把“当前页数据 + 分页信息”封装成一个对象传给 JSP，
 * JSP 只需 ${pageBean.list} 即可遍历，实现数据展示与分页控制解耦。</p>
 *
 * @param <T> 当前页数据类型
 * @author 第九小组
 */
public class PageBean<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 每页显示条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 当前页码（从 1 开始） */
    private int pageNo = 1;
    /** 每页条数 */
    private int pageSize = DEFAULT_PAGE_SIZE;
    /** 总记录数 */
    private int totalCount;
    /** 当前页数据 */
    private List<T> list;

    /** 总页数 = ceil(totalCount / pageSize) */
    public int getTotalPage() {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil(totalCount * 1.0 / pageSize);
    }

    /** 是否有上一页 */
    public boolean isHasPrev() {
        return pageNo > 1;
    }

    /** 是否有下一页 */
    public boolean isHasNext() {
        return pageNo < getTotalPage();
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo < 1 ? 1 : pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount < 0 ? 0 : totalCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
