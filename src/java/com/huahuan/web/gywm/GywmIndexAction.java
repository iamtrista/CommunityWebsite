package com.huahuan.web.gywm;

import com.huahuan.database.DatabaseAccess;
import com.huahuan.database.EasyMapsManager;
import com.huahuan.servletutil.ServletUtil;
import com.jplus.json.JSONArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Administrator
 */
@WebServlet(name = "GywmIndexAction", urlPatterns = "/GywmIndexAction.jsp")
public class GywmIndexAction extends HttpServlet {

    /**
     * 下面是模式关键字 可以自行删除和增加自定义模式，关键字一定要大写 默认模式为OTHER=0,所以OTHER不能删除
     */
    public final static int OTHER = 0;//其它
    public final static int SHOWLIST = 2;//显示列表
    
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
     * 显示成员列表
     */
    private void showlist(HttpServletRequest request, HttpServletResponse response) {
        DatabaseAccess dao = new DatabaseAccess();
        EasyMapsManager emm = new EasyMapsManager(dao);
        String sql = "SELECT"
                + " `hyb`.`xm`, `hyb`.`zw`, `hyb`.`id`, Concat(`stbm`.`bmmc`,`zwfl`.`zwmc`) as zwmc, `hyzw`.`bmid`,"
                + " `hyzw`.`zwfl`, `hyb`.`xj`"
                + " FROM"
                + " `hyb` INNER JOIN"
                + " `hyzw` ON `hyb`.`zw` = `hyzw`.`id` INNER JOIN"
                + " `stbm` ON `hyzw`.`bmid` = `stbm`.`bmid` INNER JOIN"
                + " `zwfl` ON `hyzw`.`zwfl` = `zwfl`.`zwid`"
                + " WHERE"
                + " `hyzw`.`zwfl` = 1 order by bmid desc";
        ArrayList<HashMap> hylist = emm.executeQuery(sql);
        dao.close();
        JSONArray json = new JSONArray(hylist);
        ServletUtil.ajaxData(json.toString(), response);
    }

    /**
     * 下面的模式和方法可以自行增删
     */
   
    public void execute(int event, HttpServletRequest request, HttpServletResponse response) {
        /**
         * 下面是相关模式下所做的动作*
         */
        switch (event) {
            case SHOWLIST:
                showlist(request, response);
                break;
        }
    }
}
