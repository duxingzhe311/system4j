package com.dw.web_ui.tab;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.web_ui.WebRes;

public class TabTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_TAB";
	
	private int height = 20 ;
	
	public void setHeight(String h)
	{
		if(h==null||h.equals(""))
			return ;
		
		height = Integer.parseInt(h);
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
				pageContext.getOut().write("<script src=\"/system/ui/tab.js\"></script>");
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
			String qid = "q"+t;
			String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			jw.write("<textarea id='"+nid+"' style='display:none'>") ;
			jw.write(jstxt);
			jw.write("</textarea><TABLE width='100%' border='0'  height='100%'><TR><TD id=\""+qid+"\"  height='100%' valign='top'>&nbsp;</TD></TR></TABLE>") ;
			//jstxt = Convert.plainToJsStr(jstxt);
			jw.println("<script>tab_txt_render("+nid+".value,'"+qid+"')</script>");
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}