package com.dw.system.gdb.datax.jsptag;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ListSepTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_LANG";
	
	private String lang = null ;
	
	public void setLang(String lan)
	{
		lang = lan ;
	}
//	int counts = 0;
//
//	public int doStartTag() throws JspTagException
//	{
//		System.out.println("doStartTag...");
//		if (counts > 0)
//		{
//			return BodyTag.EVAL_BODY_BUFFERED;
//		}
//		else
//		{
//			return SKIP_BODY;
//		}
//	}

//	public void setBodyContent(BodyContent bodyContent)
//	{
//		System.out.println("setBodyContent...");
//		this.bodyContent = bodyContent;
//	}
//
//	public void doInitBody() throws JspTagException
//	{
//		System.out.println("doInitBody....");
//	}

//	public int doAfterBody() throws JspTagException
//	{
//		return SKIP_BODY;
//	}

	public int doEndTag() throws JspTagException
	{
		System.out.println("do end Tag...");
		try
		{
			if (bodyContent != null)
			{
				String tmps = bodyContent.getString();
				
				if(tmps!=null&&!(tmps=tmps.trim()).equals(""))
				{
					System.out.println("body content=="+tmps);
					
					
					bodyContent.getEnclosingWriter().write(tmps);
					//bodyContent..writeOut(bodyContent.getEnclosingWriter());
				}
				
			}
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
}
