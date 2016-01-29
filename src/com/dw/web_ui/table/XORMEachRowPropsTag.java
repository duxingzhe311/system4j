package com.dw.web_ui.table;

import java.util.HashMap;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;

public class XORMEachRowPropsTag extends BodyTagSupport
{
	public static String ATTRN = "xorm_eachrow_props_";
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
		
		HashMap<String,String> propnv = null ;
		if(bodyContent!=null)
		{
			propnv = Convert.transPropStrToMap(this.bodyContent.getString()) ;
			
		}
		
		if(propnv!=null)
			pageContext.setAttribute(ATTRN, propnv);
		else
			pageContext.removeAttribute(ATTRN);
		
		return EVAL_PAGE;
	}
}
