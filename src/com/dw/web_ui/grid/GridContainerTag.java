package com.dw.web_ui.grid;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.web_ui.WebRes;

/**
 * 一个格子容器代表则一个独立的格子输出. 通过id区分
 * 生成数据的行为也以此id为基准.如初始化javascript ajax对象等
 * 
 * @author Jason Zhu
 */
public class GridContainerTag extends BodyTagSupport
{
	String id = null ;
	
	/**
	 * 在页面中的表个数--在输出多个表的时候使用
	 */
	int tableNum = 1 ;
	
	/**
	 * 当前标签运行是属于哪个表--可以用来判断本标签是第一个运行还是最后一个运行。
	 * 他根据运行方式：如果是第一个决定做ajax计算初始化，如果是最后一个就触发ajax计算输出
	 * 
	 * 因为页面脚本的现实而没办法的办法
	 */
	int tableIdx = 0 ;
	/**
	 * 
	 * @param dtv
	 */
	public void setId(String id)
	{
		this.id = id ;
	}
	
	public String getId()
	{
		return id ;
	}
	
	/**
	 * 在一个页面中的GridContainerTag出现的总次数
	 * @param tn
	 */
	public void setTable_num(String tn)
	{
		if(Convert.isNullOrEmpty(tn))
			return ;
		
		tableNum = Integer.parseInt(tn) ;
	}
	
	/**
	 * 本次标签属于总次数出现的第idx次
	 * @param ti
	 */
	public void setTable_idx(String ti)
	{
		if(Convert.isNullOrEmpty(ti))
			return ;
		
		tableIdx = Integer.parseInt(ti) ;
	}
	/**
	 * 获得该统计容器内部的 ajax显示变量名称
	 * @return
	 */
	public String getAjaxLazyShowerVar()
	{
		String alsid = (String)pageContext.getAttribute("__ajax_lazy_shower_id") ;
		if(Convert.isNotNullEmpty(alsid))
		{
			return alsid ;
		}
		alsid = "__ajax_"+UUID.randomUUID().toString().replaceAll("-", "") ;
		pageContext.setAttribute("__ajax_lazy_shower_id",alsid) ;
		return alsid ;
	}
	
	String innerDataTableVar = null ;
	
	void registerDataTableVarName(String vn)
	{
		innerDataTableVar = vn ;
	}
	
	public String getInnerDataTableVarName()
	{
		return innerDataTableVar ;
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		try
		{
			if(tableIdx==0)
			{//first time
				JspWriter jw = pageContext.getOut() ;
				jw.write("<script src='/system/ui/ajax.js'></script>");
				jw.write("<script>");
				jw.write("var "+getAjaxLazyShowerVar()+" = new AjaxLazyShower();") ;
				jw.write("</script>");
			}
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
		
		return BodyTag.EVAL_BODY_INCLUDE;
	}

	public int doEndTag() throws JspTagException
	{
		try
		{
			if(tableIdx==tableNum-1)
			{
				JspWriter jw = pageContext.getOut() ;
				jw.write("<script>");
				jw.write(getAjaxLazyShowerVar()+".show();") ;
				jw.write("</script>");
			}
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
		
		return BodyTag.EVAL_PAGE;
	}
}