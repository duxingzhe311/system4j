package com.dw.web_ui.temp;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * 作为模板的子标签运行
 * 
 * 它在启动标签之前,模板把之前的
 * @author Jason Zhu
 */
public class WebPageChildTag extends BodyTagSupport
{
	
	private String blockName = null ;
	/**
	 * 设置模板的路径,可以是相对路径/或webapp的绝对路径
	 * @param p
	 */
	public void setBlock(String p)
	{
		blockName = p ;
	}

	/**
	 * 输出模板的起始内容
	 * 
	 */
	public int doStartTag() throws JspTagException
	{
		WebPageTemplateTag wpt = (WebPageTemplateTag)this.getParent() ;
		
		try
		{
			wpt.beforeRenderBlock(blockName);
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		
		
		return BodyTag.EVAL_BODY_INCLUDE;
	}

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
		
		return EVAL_PAGE;
	}
}
