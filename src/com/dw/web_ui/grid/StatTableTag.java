package com.dw.web_ui.grid;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.web_ui.WebRes;

/**
 * 一个统计表的标签
 * 
 * @author Jason Zhu
 */
public class StatTableTag extends BodyTagSupport
{
	String dataTableVar = null ;
	
	/**
	 * 
	 * @param dtv
	 */
	public void setData_table_var(String dtv)
	{
		dataTableVar = dtv ;
	}
	
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		try
		{
			String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			JspWriter jw = pageContext.getOut() ;
			jw.write("<script type='text/javascript' src='"+CXTROOT+"WebRes?r=com/dw/web_ui/res/expand.js'></script>");
			jw.write("<script type='text/javascript' src='/system/ui/sorttable.js'></script>");
			jw.write("<style>");
			jw.write(".TableLine1  { BACKGROUND: #F3F3F3;}") ;
			jw.write(".TableLine2  { BACKGROUND: #FFFFFF;}") ;
			jw.write(".SelectedLine  { BACKGROUND: #dddddd;}") ;
			jw.write("</style>");
			String rid = UUID.randomUUID().toString() ;
			jw.println("<table id='"+rid+"' class='sortable' style='width: 100%; font-size: 10pt;margin-left: 0;margin-top: 0' border='0' cellpadding='0' cellspacing='0'>");
			
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
		
		
		return BodyTag.EVAL_BODY_INCLUDE;
	}

	public int doEndTag() throws JspTagException
	{
		return BodyTag.EVAL_PAGE;
	}
}
