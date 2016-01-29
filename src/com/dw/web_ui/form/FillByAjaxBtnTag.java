package com.dw.web_ui.form;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageTemplate;

public class FillByAjaxBtnTag extends BodyTagSupport
{
	private static WebPageTemplate templ = null ;
	
	private static WebPageTemplate getTemplate() throws IOException
	{
		if(templ!=null)
		{
			return templ;
		}
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/print/HtmlPrint.htm")) ;
		return templ ;
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
		
		String exp_info = null ;
		if(bodyContent!=null)
			exp_info = bodyContent.getString() ;
		
		String uid = UUID.randomUUID().toString().replace("-", "") ;
		
		if(exp_info==null)
		{
			return EVAL_PAGE;
		}
		
		exp_info = exp_info.trim() ;
		if(Convert.isNullOrEmpty(exp_info))
			return EVAL_PAGE ;
		
		HashMap<String,String> ps = Convert.transPropStrToMap(exp_info) ;
		//filename=aa page_ele_id= btn_title
		//
		
		
		JspWriter jw = pageContext.getOut() ;
		
		try
		{
			jw.write("<input type='button' name='' value=''") ;
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe);
		}
		
		return EVAL_PAGE;
	}
}