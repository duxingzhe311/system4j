package com.dw.web_ui.table;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;

public class XORMEachRowStyleTag extends BodyTagSupport
{
	public static String ATTRN = "xorm_eachrow_style_";
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		XORMTableTag pt = (XORMTableTag)this.getParent();
		if(!pt.getRTItem().bInLoop)
		{
			return BodyTag.SKIP_BODY;
		}
		
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	
	public int doEndTag() throws JspTagException
	{
		XORMTableTag pt = (XORMTableTag)this.getParent();
		if(!pt.getRTItem().bInLoop)
		{
			return EVAL_PAGE;
		}
		
		String s = "" ;
		if(bodyContent!=null)
		{
			s = this.bodyContent.getString() ;
			if(s==null)
				s = "" ;
			else
				s = s.trim() ;
		}
		
		pageContext.setAttribute(ATTRN, s);
		
		return EVAL_PAGE;
	}
}
