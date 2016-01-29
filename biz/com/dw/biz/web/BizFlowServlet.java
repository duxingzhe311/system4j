package com.dw.biz.web;

import java.io.IOException;
import java.io.PrintStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BizFlowServlet extends HttpServlet
{
  protected void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    HttpSession session = req.getSession();

    resp.setContentType("text/html;charset=UTF-8");
    String uri = req.getRequestURI();
    System.out.println("flow servlet uri=" + uri);
    if (!uri.endsWith(".flow")) {
      return;
    }

    String qs = req.getQueryString();
    if ((qs == null) || (qs.equals("")))
      resp.sendRedirect("/biz/biz_flow_start.jsp?fp=" + uri);
    else
      resp.sendRedirect("/biz/biz_flow_start.jsp?fp=" + uri + "&" + qs);
  }
}
