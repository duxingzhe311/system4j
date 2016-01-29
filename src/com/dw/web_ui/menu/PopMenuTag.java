package com.dw.web_ui.menu;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.web_ui.WebRes;

public class PopMenuTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_POPMENU";
	
	private String popWay = null ;
	
	private int parentLvl = 0 ;
	
	public void setPop_way(String h)
	{
		if(h==null||h.equals(""))
			return ;
		
		popWay = h;
	}
	
	public void setParent_lvl(String plvl)
	{
		if(Convert.isNullOrTrimEmpty(plvl))
			return ;
		
		parentLvl = Integer.parseInt(plvl);
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				pageContext.getOut().write("<script src=\"/system/ui/popmenu.js\"></script>");
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
			String jstxt = this.bodyContent.getString() ;
			String t = UUID.randomUUID().toString().replaceAll("-", "");
			String nid = "p"+t ;
			//String qid = "q"+t;
			//String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			jw.write("<textarea id='"+nid+"' style='display:none'>") ;
			jw.write(jstxt);
			if("right".equalsIgnoreCase(popWay))
			{
				jw.write("\r\n[option]\r\npop_way=right\r\n");
			}
			else if("left".equalsIgnoreCase(popWay))
			{
				jw.write("\r\n[option]\r\npop_way=left\r\n");
			}
			else
			{
				jw.write("\r\n[option]\r\npop_way=all\r\n");
			}
			jw.write("</textarea>") ;
			
			if(parentLvl>0)
			{
				jw.println("<script>popmenu_reg("+nid+","+parentLvl+");</script>");
			}
			else
			{
				jw.println("<script>popmenu_reg("+nid+");</script>");
			}
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}