package com.dw.web_ui.grid;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

public class GridDataTableVarTag extends BodyTagSupport
{
	public int doStartTag() throws JspTagException
	{
		//AjaxLazyShower(buffer_len)
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspTagException
	{
		String varn = this.bodyContent.getString() ;
		if(varn!=null)
			varn = varn.trim() ;
		
		if(!"".equals(varn))
		{
			GridContainerTag sct = getMyContainer() ;
			if(sct!=null)
			{
				sct.registerDataTableVarName(varn);
			}
			
			try
			{
				JspWriter jw = pageContext.getOut() ;
				jw.write("<script>var ") ;
				jw.write(varn) ;
				jw.write("= new Array() ;</script>") ;
			}
			catch(IOException ioe)
			{
				throw new JspTagException(ioe);
			}
		}
			
		return EVAL_PAGE;
	}
	
	private GridContainerTag getMyContainer()
	{
		Tag t = null ;
		
		while((t=this.getParent())!=null)
		{
			if(t instanceof GridContainerTag)
			{
				return (GridContainerTag)t ;
			}
		}
		
		return null;
	}
}
