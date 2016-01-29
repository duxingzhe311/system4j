package com.dw.web_ui.table;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * ��xormTable�ڲ���Query����, �� where Id>10��
 * @author Jason Zhu
 */
public class XORMWhereTag extends BodyTagSupport
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

	
	// public void setBodyContent(BodyContent bodyContent)
	// {
	// System.out.println("setBodyContent...");
	// this.bodyContent = bodyContent;
	// }
	//
	// public void doInitBody() throws JspTagException
	// {
	// System.out.println("doInitBody....");
	// }

	// public int doAfterBody() throws JspTagException
	// {
	// return SKIP_BODY;
	// }

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