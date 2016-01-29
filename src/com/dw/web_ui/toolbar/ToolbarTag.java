package com.dw.web_ui.toolbar;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.DataTable;
import com.dw.system.*;
import com.dw.web_ui.WebRes;

public class ToolbarTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_TOOL_BAR";
	
	private int height = 20 ;
	
	private String trCss = null ;
	
	public void setHeight(String h)
	{
		if(h==null||h.equals(""))
			return ;
		
		height = Integer.parseInt(h);
	}
	
	public void setTr_css(String trcss)
	{
		trCss = trcss;
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				//pageContext.getOut().write("<script src=\"/system/ui/page.js\"></script>");
				pageContext.getOut().write("<script src=\"/system/ui/main.js\"></script>");
				pageContext.getOut().write("<script src=\"/system/ui/4funtoolbar.js\"></script>");
				pageContext.setAttribute(CXT_ATTRN, "true");
			}
			catch(IOException ioe)
			{
				throw new JspTagException(ioe);
			}
		}
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	
	public int doEndTag() throws JspTagException
	{
		try
		{
			JspWriter jw = pageContext.getOut() ;
			String jstxt = this.bodyContent.getString() ;
			String t = UUID.randomUUID().toString().replaceAll("-", "");
			String nid = "p"+t ;
			String qid = "q"+t;
			String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			jw.write("<table border='0' cellpadding='0' cellspacing='0' width='100%' height0='100%' style='width: 100%; font-size: 10pt;margin-left: 0;margin-top: 0'>");
			if(trCss!=null)
				jw.write("<tr class='"+trCss+"' height='"+height+"'>\r\n");
			else
				jw.write("<tr style='border-bottom: 1px solid rgb(104, 104, 104); background-image: url("+CXTROOT+"WebRes?r=com/dw/web_ui/res/tool-bkgd.jpg);' height='"+height+"'>\r\n");
			jw.write("<td colspan='30'>") ;
			jw.write("<textarea id='"+nid+"' style='display:none'>") ;
			jw.write(jstxt);
			jw.write("</textarea><div id=\""+qid+"\"></div>") ;
			//jstxt = Convert.plainToJsStr(jstxt);
			
			jw.println("<script>f23('toolbar',document.getElementById('"+nid+"').value,'"+qid+"')</script>");
			jw.println("</td></tr></table>\r\n") ;
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}