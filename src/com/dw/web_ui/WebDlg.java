package com.dw.web_ui;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.*;

public class WebDlg extends HttpServlet
{
	protected void service(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException
	{
		String u = req.getParameter("u");
		PrintWriter pw = resp.getWriter() ;
		pw.write("<html><body style='margin: 0;border: 0'>") ;
		pw.write("<iframe scrolling='auto' frameborder='0' src='"+u+"' width='100%' height='100%'></iframe>") ;
		pw.write("</body>") ;
		pw.write("</html>") ;
	}
}
