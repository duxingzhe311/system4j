package com.dw.web_ui.table;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class XORMDBAccess extends BodyTagSupport
{
	/**
	 * ���ģ�����ʼ����
	 */
	public int doStartTag() throws JspTagException
	{
		XORMTableTag pt = (XORMTableTag)this.getParent() ;
		if(pt.getRTItem().bInLoop)
			return BodyTag.SKIP_BODY ;
		
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspTagException
	{
		XORMTableTag pt = (XORMTableTag)this.getParent() ;
		if(!pt.getRTItem().bInLoop)
		{
			String s = this.bodyContent.getString() ;
			if(s!=null)
				pt.getRTItem().whereStr = s.trim();
		}
		
		return EVAL_PAGE;
	}
}
