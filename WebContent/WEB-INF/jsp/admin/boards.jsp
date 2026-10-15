<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<%-- ===================== 后台 · 板块管理 =====================
     数据由 AdminServlet 提供：boards（含 postCount） --%>

<div class="container admin-wrap">
    <div class="admin-head">
        <h2><svg class="i"><use href="#i-grid"></use></svg>板块管理</h2>
        <span class="desc">新增 / 编辑 / 删除板块，删除时可把帖子合并到其他板块</span>
        <div class="admin-nav">
            <a class="sort" href="${ctx}/admin?op=dashboard">数据统计</a>
            <a class="sort" href="${ctx}/admin?op=posts">内容管理</a>
            <a class="sort on" href="${ctx}/admin?op=boards">板块管理</a>
            <a class="sort" href="${ctx}/admin?op=replies">回复管理</a>
            <a class="sort" href="${ctx}/admin?op=users">用户管理</a>
            <a class="sort" href="${ctx}/posts">返回前台</a>
        </div>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="alert ok"><c:out value="${sessionScope.flash}"/></div>
        <c:remove var="flash" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert error"><c:out value="${sessionScope.flashError}"/></div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <%-- 新增板块 --%>
    <div class="card form-card">
        <h3 class="card-title">新增板块</h3>
        <form action="${ctx}/admin?op=boards" method="post">
            <input type="hidden" name="action" value="addBoard">
            <div class="field-row">
                <label>名称<input type="text" name="name" maxlength="20" placeholder="如：综合讨论" required></label>
                <label>图标<textarea name="icon" rows="1" maxlength="8" placeholder="emoji 或短文本，如 💬 或 综"></textarea></label>
                <label>排序<input type="number" name="sortOrder" value="0" style="width:90px"></label>
            </div>
            <label class="full">简介<textarea name="description" rows="2" maxlength="120" placeholder="一句话描述这个板块是干什么的"></textarea></label>
            <div class="form-actions"><button class="btn primary" type="submit">添加板块</button></div>
        </form>
    </div>

    <%-- 编辑板块（点击“编辑”后由 JS 填充并显示） --%>
    <div class="card form-card" id="editCard" style="display:none">
        <h3 class="card-title">编辑板块</h3>
        <form action="${ctx}/admin?op=boards" method="post">
            <input type="hidden" name="action" value="editBoard">
            <input type="hidden" name="id" id="editId">
            <div class="field-row">
                <label>名称<input type="text" name="name" id="editName" maxlength="20" required></label>
                <label>图标<textarea name="icon" id="editIcon" rows="1" maxlength="8"></textarea></label>
                <label>排序<input type="number" name="sortOrder" id="editSort" style="width:90px"></label>
            </div>
            <label class="full">简介<textarea name="description" id="editDesc" rows="2" maxlength="120"></textarea></label>
            <div class="form-actions">
                <button class="btn primary" type="submit">保存修改</button>
                <button class="btn ghost" type="button" onclick="document.getElementById('editCard').style.display='none'">取消</button>
            </div>
        </form>
    </div>

    <table class="tbl">
        <thead>
        <tr>
            <th style="width:52px">ID</th>
            <th style="width:70px">图标</th>
            <th>名称</th>
            <th>简介</th>
            <th style="width:90px">帖子数</th>
            <th style="width:220px">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty boards}">
                <tr><td colspan="6" style="text-align:center;padding:40px" class="muted">暂无板块</td></tr>
            </c:when>
            <c:otherwise>
                <c:forEach items="${boards}" var="b">
                    <tr>
                        <td><c:out value="${b.id}"/></td>
                        <td><span class="ic-lg"><jsp:include page="/WEB-INF/jsp/common/board-icon.jsp"><jsp:param name="name" value="${b.name}"/><jsp:param name="icon" value="${b.icon}"/></jsp:include></span></td>
                        <td><c:out value="${b.name}"/></td>
                        <td class="muted" style="font-size:13px"><c:out value="${b.description}"/></td>
                        <td><c:out value="${b.postCount}"/></td>
                        <td>
                            <div class="tbl-actions">
                                <button class="btn ghost sm" type="button"
                                        onclick="editBoard(${b.id},'<c:out value="${b.name}"/>','<c:out value="${b.icon}"/>',<c:out value="${b.sortOrder}"/>,'<c:out value="${b.description}"/>')">编辑</button>
                                <form action="${ctx}/admin?op=boards" method="post"
                                      onsubmit="return confirm('确定删除该板块？可在下方选择把帖子合并到其他板块')">
                                    <input type="hidden" name="action" value="delBoard">
                                    <input type="hidden" name="id" value="${b.id}">
                                    <select name="mergeTo" class="merge-sel">
                                        <option value="0">删除帖子</option>
                                        <c:forEach items="${boards}" var="b2">
                                            <c:if test="${b2.id != b.id}">
                                                <option value="${b2.id}">并入：${b2.name}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                    <button class="btn danger sm" type="submit">删除</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>

<script>
    function editBoard(id, name, icon, sort, desc) {
        document.getElementById('editId').value = id;
        document.getElementById('editName').value = name;
        document.getElementById('editIcon').value = icon;
        document.getElementById('editSort').value = sort;
        document.getElementById('editDesc').value = desc;
        var card = document.getElementById('editCard');
        card.style.display = 'block';
        card.scrollIntoView({behavior: 'smooth', block: 'center'});
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
