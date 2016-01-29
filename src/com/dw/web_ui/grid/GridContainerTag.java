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
 * һ����������������һ�������ĸ������. ͨ��id����
 * �������ݵ���ΪҲ�Դ�idΪ��׼.���ʼ��javascript ajax�����
 * 
 * @author Jason Zhu
 */
public class GridContainerTag extends BodyTagSupport
{
	String id = null ;
	
	/**
	 * ��ҳ���еı����--�����������ʱ��ʹ��
	 */
	int tableNum = 1 ;
	
	/**
	 * ��ǰ��ǩ�����������ĸ���--���������жϱ���ǩ�ǵ�һ�����л������һ�����С�
	 * ���������з�ʽ������ǵ�һ��������ajax�����ʼ������������һ���ʹ���ajax�������
	 * 
	 * ��Ϊҳ��ű�����ʵ��û�취�İ취
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
	 * ��һ��ҳ���е�GridContainerTag���ֵ��ܴ���
	 * @param tn
	 */
	public void setTable_num(String tn)
	{
		if(Convert.isNullOrEmpty(tn))
			return ;
		
		tableNum = Integer.parseInt(tn) ;
	}
	
	/**
	 * ���α�ǩ�����ܴ������ֵĵ�idx��
	 * @param ti
	 */
	public void setTable_idx(String ti)
	{
		if(Convert.isNullOrEmpty(ti))
			return ;
		
		tableIdx = Integer.parseInt(ti) ;
	}
	/**
	 * ��ø�ͳ�������ڲ��� ajax��ʾ��������
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
	 * ���ģ�����ʼ����
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