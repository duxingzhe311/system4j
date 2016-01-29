package com.dw.web_ui.temp;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * ��Ϊģ����ӱ�ǩ����
 * 
 * ����������ǩ֮ǰ,ģ���֮ǰ��
 * @author Jason Zhu
 */
public class WebPageChildTag extends BodyTagSupport
{
	
	private String blockName = null ;
	/**
	 * ����ģ���·��,���������·��/��webapp�ľ���·��
	 * @param p
	 */
	public void setBlock(String p)
	{
		blockName = p ;
	}

	/**
	 * ���ģ�����ʼ����
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
