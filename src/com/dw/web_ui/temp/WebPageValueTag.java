package com.dw.web_ui.temp;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.*;

/**
 * ��WebPageTemplateTag��,������ģ���е�[##]
 * 
 * @author Jason Zhu
 */
public class WebPageValueTag extends BodyTagSupport
{
	
	private String blockName = null ;
	
	private String blockVal = "" ;
	/**
	 * ����ģ���·��,���������·��/��webapp�ľ���·��
	 * @param p
	 */
	public void setBlock(String p)
	{
		blockName = p ;
	}

	public void setValue(String val)
	{
		blockVal = val ;
		if(blockVal==null)
			blockVal = "" ;
	}
	/**
	 * ���ģ�����ʼ����
	 * 
	 */
	public int doStartTag() throws JspTagException
	{
		
		return BodyTag.SKIP_BODY;
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
		WebPageTemplateTag wpt = (WebPageTemplateTag)this.getParent() ;
		
		try
		{
			wpt.beforeRenderBlock(blockName).write(blockVal);
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		
		return EVAL_PAGE;
	}
}
