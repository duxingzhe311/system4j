package com.dw.web_ui;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;

public class WebInputFilesTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_INPUT_FILES";
	
	private static String INPUT_FILE_HELP_TXT = null ;
	
	private static String getHelpTxt() throws IOException
	{
		if(INPUT_FILE_HELP_TXT!=null)
			return INPUT_FILE_HELP_TXT;

		INPUT_FILE_HELP_TXT = WebRes.getResTxtContent("com/dw/web_ui/res/input_files_help.txt");
		return INPUT_FILE_HELP_TXT;
	}
	
	private String height = "150" ;
	
	private String selectFileTitle = "Select" ;
	
	private String selectPos = "top" ;
	
	public void setHeight(String h)
	{
		height = h ;
	}
	
	public void setSelect_title(String st)
	{
		if(Convert.isNullOrTrimEmpty(st))
			return ;
		
		selectFileTitle = st ;
	}
	
	public void setSelect_pos(String p)
	{
		if(Convert.isNullOrTrimEmpty(p))
			return ;
		
		selectPos = p ;
	}
	
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				String ht = getHelpTxt() ;
				pageContext.getOut().write(ht);
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
		//System.out.println("do end Tag...");
		try
		{
			//String tmps = bodyContent.getString();
				
				JspWriter jw = pageContext.getOut() ;
				
				jw.write("<table border='0' cellpadding='0' cellspacing='0' width='100%'>") ;
				jw.write("<tr>") ;
				jw.write("<td>") ;
				if(!"buttom".equalsIgnoreCase(selectPos))
					jw.write("    <a href='javascript:AddAttachment()'>"+selectFileTitle+"</a>") ;
				jw.write("    <div id='attach_div' style='border-width:1px;border-style:solid;height:"+height+"px;overflow:scroll;'>") ;
				jw.write("       <div id='attach_paths'>") ;
				jw.write("        </div><div style='height:100%;width:100%' onclick='AddAttachment()' ></div>") ;
				jw.write("    </div>");
				if("buttom".equalsIgnoreCase(selectPos))
					jw.write("    <a href='javascript:AddAttachment()'>"+selectFileTitle+"</a>") ;
				
				jw.write("    </td>");
				jw.write("</tr>");
				jw.write("<tr>") ;
				jw.write("    <td>&nbsp;</td>");
				jw.write("</tr>") ;
				jw.write("</table>") ;
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
}
