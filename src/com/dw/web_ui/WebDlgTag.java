package com.dw.web_ui;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * 支持页面div中IFrame的方式展现弹出对话框的标签
 * 
 * 
 * @author Jason Zhu
 */
public class WebDlgTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_LANG";
	
	private String url = null ;
	private int width = 300 ;
	private int height = 300 ;
	
	public void setUrl(String url)
	{
		this.url = url ;
	}
	
	public void setWidth(int w)
	{
		if(w>0)
			width = w ;
	}
	
	public void setHeight(int h)
	{
		if(h>0)
			height = h ;
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
		//System.out.println("do end Tag...");
		try
		{
			if (bodyContent != null)
			{
				String tmps = bodyContent.getString();
				
				if(tmps!=null&&!(tmps=tmps.trim()).equals(""))
				{
					//System.out.println("body content=="+tmps);
					WebPageLang wpl = (WebPageLang)this.pageContext.getAttribute(CXT_ATTRN) ;
					if(wpl==null)
					{
						wpl = WebPageLang.getPageLang((Servlet)pageContext.getPage(),(HttpServletRequest)pageContext.getRequest());
						if(wpl!=null)
							pageContext.setAttribute(CXT_ATTRN, wpl);
					}
					
//					if(wpl!=null)
//						this.pageContext.getOut().write(wpl.getLangValue(tmps,lang));
//					else
//						this.pageContext.getOut().write(tmps);
					bodyContent.writeOut(bodyContent.getEnclosingWriter());
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
