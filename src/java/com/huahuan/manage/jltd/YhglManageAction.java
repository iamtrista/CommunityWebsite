package com.huahuan.manage.jltd;

import com.huahuan.mailutil.SendMail;
import com.huahuan.servletutil.ServletUtil;
import com.huahuan.table.Qxlb;
import com.huahuan.table.View_yhb;
import com.huahuan.table.Yhb;
import com.huahuan.tools.CutJsonString;
import com.huahuan.tools.MD5;
import com.huahuan.tools.Util;
import com.jplus.json.EasyUiJson;
import com.jplus.json.JSONArray;
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
@WebServlet(name = "YhglManageAction", urlPatterns = "/manage/YhglManageAction.jsp")
public class YhglManageAction extends HttpServlet {

    /**
     * 下面是模式关键字 可以自行删除和增加自定义模式，关键字一定要大写 默认模式为OTHER=0,所以OTHER不能删除
     */
    public final static int OTHER = 0;//其它
    public final static int SHOWONE = 1;//显示单例
    public final static int SHOWLIST = 2;//显示列表
    public final static int UPDATE = 4;//更新记录
    public final static int EDIT = 5;//编辑刻录
    public final static int DELETE = 6;//删除记录
    public final static int LOADYHQXCOMBO = 7;
    public final static int UPDATETP = 8;
    public final static int SENDMAIL = 9;
    public final static int SENDALL = 10;

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
     * 显示用户信息的列表
     */
    private void showlist(HttpServletRequest request, HttpServletResponse response) {
        //禁止json缓存
        response.setHeader("pragma", "no-cache");
        response.setHeader("cache-control", "no-cache");
        response.setHeader("expires", "0");
        String searchValue = request.getParameter("searchValue");
        String searchName = request.getParameter("searchName");
        View_yhb yhb = new View_yhb();
        Hyberbin hyb = new Hyberbin(yhb);
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
     * 删除多个用户
     */
    private void delete(HttpServletRequest request, HttpServletResponse response) {//这里的删除量过大，采用事务处理比较安全
        String num = request.getParameter("ids");//用于批量删除拼接的id字符串
        Yhb yhb = new Yhb();
        Hyberbin hyb = new Hyberbin(yhb, true);
        String[] nums = num.split(":");
        String sql = "";
        for (int i = 0; i < nums.length; i++) {//得到所要删除的用户的id
            hyb.addParmeter(nums[i]);
            sql += " or id=?";
        }
        sql = sql.substring(3);
        boolean b = hyb.dell("where" + sql);
        hyb.reallyClose();
        String message = b ? "操作成功" : "操作失败";
        ServletUtil.ajaxData(message, response, "html");
    }

    /**
     * 更改一个用户的信息
     */
    private void update(HttpServletRequest request, HttpServletResponse response) {
        Yhb yhb = new Yhb();//论坛用户表
        Hyberbin hyb = new Hyberbin(yhb);
        ServletUtil.loadByBean(request, yhb, true);
        String mm = request.getParameter("xmm");
        if (!Util.isEmpty(mm)) {//如果密码非空，则修改
            yhb.setMm(MD5.jplusMd5(mm));
        }
        boolean b = hyb.updateByKey("id");
        String message = b ? "操作成功" : "操作失败";
        ServletUtil.ajaxData(message, response, "html");
    }

    /**
     * 加载用户权限的下拉框
     */
    private void loadYhqxCombo(HttpServletRequest request, HttpServletResponse response) {
        Qxlb qxlb = new Qxlb();
        Hyberbin hyb = new Hyberbin(qxlb);
        List list = hyb.showList("select qxid,qxmc from qxlb");
        JSONArray json = new JSONArray();
        json.put(list);
        ServletUtil.ajaxData(CutJsonString.cutComboJson(json.toString()), response);
    }

    /**
     * 修改用户头像
     */
    private void updatetp(HttpServletRequest request, HttpServletResponse response) {
        String newPicPath = request.getParameter("newPicPath");
        String id = request.getParameter("id");
        Yhb sthd = new Yhb();//会员表
        Hyberbin hyberbin = new Hyberbin(sthd);
        sthd.setYhtx(newPicPath);
        sthd.setId(Integer.parseInt(id));
        boolean b = hyberbin.updateByKey("id");
        String str = b ? "修改成功" : "修改失败";
        ServletUtil.ajaxData("{\"notice\":\"" + str + "\"}", response);
    }

    /**
     * 给指定的用户群发送邮件
     */
    public void SendMailAax(HttpServletRequest request, HttpServletResponse response) {
        String mailStr = request.getParameter("mailStr");
        String constant = request.getParameter("constant");
        String[] mails = mailStr.split(":");
        String title = "您好，这是\"湖师担当者志愿社\"系统邮件！";
        SendMail.sendGroupEmail(request, mails, title, constant, 1);
        ServletUtil.ajaxData(mails.length + "位用户的邮件发送成功！", response, "html");
    }

    /**
     * 给所有用户群发邮件
     */
    public void sendAllMail(HttpServletRequest request, HttpServletResponse response) {
        String constant = request.getParameter("constant");
        String title = "你好，这是\"湖师担当者分社\"系统邮件！";
        Hyberbin hyberbin = new Hyberbin(new Yhb());
        List showList = hyberbin.showList("select yx from yhb where sfjh=1");
        String[] sendList = null;
        for (int i = 0; i < showList.size(); i++) {
            if (i % 10 == 0) {
                sendList = new String[10];
            }
            Yhb yhb = (Yhb) showList.get(i);
            sendList[i % 10] = yhb.getYx();
            if (i % 10 == 0) {
                SendMail.sendGroupEmail(request, sendList, title, constant, 1);
            }
        }
        ServletUtil.ajaxData(showList.size() + "位用户的邮件发送成功！", response, "html");
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
            case UPDATE:
                update(request, response);
                break;
            case DELETE:
                delete(request, response);
                break;
            case LOADYHQXCOMBO:
                loadYhqxCombo(request, response);
                break;
            case UPDATETP:
                updatetp(request, response);
                break;
            case SENDMAIL:
                SendMailAax(request, response);
                break;
            case SENDALL:
                sendAllMail(request, response);
                break;
        }
    }
}
