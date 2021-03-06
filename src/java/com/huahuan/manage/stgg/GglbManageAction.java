package com.huahuan.manage.stgg;

import com.huahuan.servletutil.ServletUtil;
import com.huahuan.table.Gglb;
import com.huahuan.table.Stgg;
import com.huahuan.tools.Util;
import com.jplus.json.EasyUiJson;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jplus.hyb.database.Hyberbin;

/**
 *
 * @author Administrator
 */
@WebServlet(name = "GglbManageAction", urlPatterns = "/manage/GglbManageAction.jsp")
public class GglbManageAction extends HttpServlet {

    /**
     * 下面是模式关键字 可以自行删除和增加自定义模式，关键字一定要大写 默认模式为OTHER=0,所以OTHER不能删除
     */
    public final static int OTHER = 0;//其它
    public final static int SHOWLIST = 2;//显示列表
    public final static int ADD = 3;//添加记录
    public final static int UPDATE = 4;//更新记录
    public final static int DELETE = 6;//删除记录

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        execute(ServletUtil.setModel(request.getParameter("mode"), this), request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * 显示协会公告类别列表
     */
    private void showlist(HttpServletRequest request, HttpServletResponse response) {
        //禁止json缓存
        response.setHeader("pragma", "no-cache");
        response.setHeader("cache-control", "no-cache");
        response.setHeader("expires", "0");
        String searchValue = request.getParameter("searchValue");
        String searchName = request.getParameter("searchName");
        Gglb gglb = new Gglb();
        Hyberbin hyb = new Hyberbin(gglb);
        List list;
        EasyUiJson easyui = new EasyUiJson(request);
        String where = "";
        if (Util.isEmpty(searchValue)) {
            list = hyb.showByMySqlPage(where, easyui);
        } else {
            where = " where " + searchName + " like ?";
            hyb.addParmeter("%" + searchValue + "%");
            list = hyb.showByMySqlPage(where, easyui);
        }
        easyui.putAll(list);
        ServletUtil.ajaxData(easyui.toDataString(), response);
    }

    /**
     * 删除公告类别,删除量比较大
     */
    private void deleteGglb(HttpServletRequest request, HttpServletResponse response) {
        boolean b = false;
        String num = request.getParameter("ids");//用于批量删除拼接的id字符串
        Stgg stgg = new Stgg();
        Gglb gglb = new Gglb();
        Hyberbin hyb = new Hyberbin(stgg, true);
        String[] nums = num.split(":");
        String lbsql = Util.getDeleteSql(hyb, nums, "gglb");
        List gglist = hyb.showList("select ggid,gglb from stgg where" + lbsql);
        if (gglist == null || gglist.isEmpty()) {//如果没有公告,则只需要删除类别
            hyb.changeTable(gglb);
            lbsql = "";
            lbsql = Util.getDeleteSql(hyb, nums, "lbid");
            b = hyb.dell("where" + lbsql);
        } else {//否则要先删除类别下公告,才能删除类别
            hyb.clearParmeter();
            String ggsql = "";
            for (int i = 0; i < gglist.size(); i++) {//得到与公告类别id有关的所有公告的id
                Stgg temp = (Stgg) gglist.get(i);
                hyb.addParmeter(temp.getGgid());
                ggsql += " or ggid=?";
            }
            ggsql = ggsql.substring(3);
            hyb.dell("where" + ggsql);
            hyb.changeTable(gglb);
            lbsql = "";
            lbsql = Util.getDeleteSql(hyb, nums, "lbid");
            b = hyb.dell("where" + lbsql);
        }
        hyb.reallyClose();
        String message = b ? "操作成功" : "操作失败";
        ServletUtil.ajaxData(message, response);
    }

    /**
     * 添加协会公告类别
     */
    private void addGglb(HttpServletRequest request, HttpServletResponse response) {
        Gglb gglb = new Gglb();
        Hyberbin hyberbin = new Hyberbin(gglb);
        ServletUtil.loadByBean(request, gglb, true);
        boolean b = hyberbin.insert("lbid");
        String message = b ? "操作成功" : "操作失败";
        ServletUtil.ajaxData(message, response, "html");
    }

    /**
     * 修改协会公告类别
     */
    private void updateGglb(HttpServletRequest request, HttpServletResponse response) {
        Gglb gglb = new Gglb();//社团公告类别表
        Hyberbin hyberbin = new Hyberbin(gglb);
        ServletUtil.loadByBean(request, gglb, true);
        boolean b = hyberbin.updateByKey("lbid");
        String message = b ? "操作成功" : "操作失败";
        ServletUtil.ajaxData(message, response, "html");
    }

    /**
     * 实现父类的抽象方法，下面的模式和方法可以自行增删
     */
    public void execute(int event, HttpServletRequest request, HttpServletResponse response) {
        /**
         * 下面是相关模式下所做的动作*
         */
        switch (event) {
            case SHOWLIST:
                showlist(request, response);
                break;
            case ADD:
                addGglb(request, response);
                break;
            case UPDATE:
                updateGglb(request, response);
                break;
            case DELETE:
                deleteGglb(request, response);
                break;
        }
    }
}
