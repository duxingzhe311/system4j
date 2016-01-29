package com.dw.biz.web;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizManager;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BizActionServlet extends HttpServlet
{
  static BizManager bizMgr = BizManager.getInstance();

  protected void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    HttpSession session = req.getSession();

    resp.setContentType("text/html;charset=UTF-8");
    String uri = req.getRequestURI();
    if ((!uri.endsWith(".action")) && (!uri.endsWith(".ba"))) {
      return;
    }
    try
    {
      XmlData xd = XORMUtil.getXmlDataFromRequest(req);

      UserProfile up = UserProfile.getUserProfile(req);
      BizActionResult bar = bizMgr.RT_doBizAction(up, uri, null, xd);
      XmlData outputxd = bar.getResultData();

      PrintWriter pw = resp.getWriter();
      pw.print(bar.getResultStr());
    }
    catch (Exception e)
    {
      throw new ServletException("Do Biz Action Servlet Error:" + e.getMessage());
    }
  }
}
