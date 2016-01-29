package com.dw.web_ui.editor;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import com.dw.system.Convert;
import com.dw.web_ui.WebUtil;
import com.dw.web_ui.grid.GridContainerTag;
import com.dw.web_ui.grid.GridValueTag.ValueItem;

public class SmartEditorNameValueTag extends BodyTagSupport
{
	public static final int TYPE_MAIN_CONT = 0 ;
	public static final int TYPE_BLOCK_CONT = 1 ;
	public static final int TYPE_SUBMIT_PARAM = 2 ;
	
	private String name = null ;
	private int type = TYPE_MAIN_CONT ;
	
	public void setName(String bn)
	{
		name = bn ;
	}
	
	public void setType(String t)
	{
		if("block".equalsIgnoreCase(t))
			type = TYPE_BLOCK_CONT ;
		else if("param".equalsIgnoreCase(t))
			type = TYPE_SUBMIT_PARAM ;
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		//AjaxLazyShower(buffer_len)
		return BodyTag.EVAL_BODY_BUFFERED;
	}
	

	public int doEndTag() throws JspTagException
	{
		String inputs = null;
		if(this.bodyContent!=null)
			inputs = bodyContent.getString() ;
		if(inputs==null)
			inputs = "" ;
		
		SmartEditorTag se = (SmartEditorTag)this.getParent() ;
		if(se!=null)
		{
			switch(type)
			{
			case TYPE_BLOCK_CONT:
				se.setBlockContent(name, inputs) ;
				break ;
			case TYPE_SUBMIT_PARAM:
				se.setParamValue(name,inputs) ;
				break ;
			default:
				se.setMainCont(inputs) ;
				break ;
			}
		}
		return EVAL_PAGE;
	}
}