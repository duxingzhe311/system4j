package com.dw.biz.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BizFuncServlet extends HttpServlet
{
  protected void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    HttpSession session = req.getSession();

    resp.setContentType("text/html;charset=UTF-8");
    String uri = req.getRequestURI();
    if ((!uri.endsWith(".func")) && (!uri.endsWith(".bf"))) {
      return;
    }

    resp.sendRedirect("/biz/run_biz_func.jsp?p=" + uri);
  }
}
