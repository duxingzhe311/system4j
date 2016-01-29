package com.dw.web_ui;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.AppConfig;
import com.dw.system.Convert;

public class WebPageLangTag extends BodyTagSupport
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
	
	/**
	 * 根据当前的页面上下文,获得key对应的语言值
	 * @throws JspTagException 
	 */
	public static String getPageLangValue(PageContext pc,String key) throws JspTagException
	{
		//String lan = AppConfig.getAppLang(pc);
		WebPageLangFileTag.PageLangBuffer plb = WebPageLangFileTag.getOrCreatePageLangBuffer(pc);
		//WebPageLang wpl = (WebPageLang)this.pageContext.getAttribute(CXT_ATTRN) ;
		return plb.getLangValue(key);
		//System.out.println("lang ["+lan+"]>>>>"+tmps+"="+vv);
	}

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
					String vv = getPageLangValue(pageContext,tmps);
					this.pageContext.getOut().write(vv);
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
