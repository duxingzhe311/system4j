package com.dw.web_ui;

import java.io.IOException;
import java.util.*;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.system.xmldata.XmlVal;

public class DateInputTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_INPUT_DATE";
	
	private String input_i = "";
	private String input_n = "";
	private String input_v = "";
	private String input_s = "";
	
	private boolean bShowDetail = false;
	
	public void setInput_id(String inputi)
	{
		if(inputi == null || inputi.equals("") )
			return;
		
		this.input_i = inputi;
	}
	
	public void setInput_name(String inputn)
	{
		if(inputn == null || inputn.equals("") )
			return;
		
		this.input_n = inputn;
	}
	
	public void setInput_value(String input_v)
	{
		if(input_v == null || input_v.equals("") )
			return;
		
		this.input_v = input_v;
	}
	
	public void setInput_size(String input_s)
	{
		if(input_s == null || input_s.equals("") )
			return;
		
		this.input_s = input_s;
	}
	
	public void setShow_detail(String sd)
	{
		bShowDetail = "true".equalsIgnoreCase(sd)||"1".equals(sd)||"yes".equalsIgnoreCase(sd);
	}
	
	
	/**
	 * 输出模板的起始内�?
	 */
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				pageContext.getOut().write("<script type=\"text/javascript\" src=\"/system/ui/dlg.js\"></script>");
				pageContext.getOut().write("<script type=\"text/javascript\" src=\"/system/ui/input_date.js\"></script>");
				pageContext.getOut().write("<script type=\"text/javascript\" src=\"/system/js/biz_view.js\"></script>");
				pageContext.setAttribute(CXT_ATTRN, "true");
				
			}
			catch(IOException ioe)
			{
				throw new JspTagException(ioe);
			}
		}
		return BodyTag.EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspTagException
	{
		try
		{
			JspWriter jw = pageContext.getOut() ;
			
			if(input_s==null||input_s.equals(""))
				input_s = "15px";
			
			String v = "" ;
			if(Convert.isNotNullEmpty(input_v))
			{
				v = input_v ;
			}
			else
			{
				if(bodyContent!=null)
				{
					String tmps = bodyContent.getString();
					if(tmps!=null&&!(tmps=tmps.trim()).equals(""))
						v = tmps ;
				}
			}
			
			String input_val = "" ;
			String input_dis = "" ;
			if(Convert.isNotNullEmpty(v))
			{
				Calendar cal = Convert.toCalendar(v);
				if(cal!=null)
				{
					input_val = XmlVal.transDateToValStr(cal.getTime()) ;
					if(bShowDetail)
					{
						input_dis = Convert.toFullYMDHMS(cal.getTime());
					}
					else
					{
						input_dis = Convert.toShortYMD(cal.getTime());
					}
				}
			}
			
			String tmpid = input_i ;
			if(Convert.isNullOrEmpty(tmpid))
				tmpid = input_n ;
			
			jw.write("<input ID='" + tmpid + "' type='hidden' name='" + input_n + "' value='" + input_val +"' />");
			jw.write("<input ID='Names_"+ tmpid + "' size='" + input_s + "' name='Names_" + input_n + "' value='" + input_dis + "' readonly='readonly'/>");
			if(bShowDetail)
			{
				jw.write("<input ID='htmlBtnSelectDate_" + input_n + "' type='button'  value='...' " +
						"onclick=\"javascript:getDateXmlValBySelectDlg(document.getElementById('" + tmpid + "'),null,document.getElementById('Names_" + tmpid + "'),true)\" />");
			}
			else
			{
				jw.write("<input ID='htmlBtnSelectDate_" + input_n + "' type='button'  value='...' " +
						"onclick=\"javascript:getDateXmlValBySelectDlg(document.getElementById('" + tmpid + "'),document.getElementById('Names_" + tmpid + "'))\" />");
			}
			
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
	
	
}

