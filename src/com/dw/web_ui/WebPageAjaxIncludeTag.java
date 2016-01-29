package com.dw.web_ui;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class WebPageAjaxIncludeTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_AJAX_INC";
	
	private String lang = null ;
	
	public void setLang(String lan)
	{
		lang = lan ;
	}
//	int counts = 0;
//
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				pageContext.getOut().write("<script src=\"/system/ui/page.js\"></script>");
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
		//System.out.println("do end Tag...");
		try
		{
			if (bodyContent != null)
			{
				String tmps = bodyContent.getString();
				if(tmps==null||(tmps=tmps.trim()).equals(""))
					return EVAL_PAGE ;
				
				String nid = "p"+UUID.randomUUID().toString().replaceAll("-", "");
				pageContext.getOut().write("<div id=\""+nid+"\"></div>");
				pageContext.getOut().write("<script>page_render('"+tmps+"','"+nid+"')</script>");
			}
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
}