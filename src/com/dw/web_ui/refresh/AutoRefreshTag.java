package com.dw.web_ui.refresh;

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

public class AutoRefreshTag extends BodyTagSupport
{
	private static WebPageTemplate templ = null ;
	
	private static WebPageTemplate getTemplate() throws IOException
	{
		if(templ!=null)
		{
			return templ;
		}
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/refresh/HtmlRefresh.htm")) ;
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
		HashMap<String,String> ps = Convert.transPropStrToMap(exp_info) ;
		
		String eleid = ps.get("id") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("ids") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("ele_id") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("ele_ids") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("page_ele_id") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("page_ele_ids") ;
		if(Convert.isNullOrEmpty(eleid))
			throw new JspTagException("no page element id found in tag");
		
		StringBuilder sb = new StringBuilder();
		if(Convert.isNotNullEmpty(eleid))
		{
			StringTokenizer st = new StringTokenizer(eleid,",|") ;
			if(st.hasMoreTokens())
			{
				sb.append('\'').append(st.nextToken()).append('\'') ;
			}
			
			while(st.hasMoreTokens())
			{
				sb.append(",\'").append(st.nextToken()).append('\'') ;
			}
		}
		
		String eleids = sb.toString() ;
		
		String interval = ps.get("interval");
		if(Convert.isNullOrEmpty(interval))
			throw new JspTagException("no page element refresh interval found in tag");
		
		long l_interval = 0 ;
		interval = interval.toLowerCase() ;
		int len = interval.length() ;
		if(interval.endsWith("ms"))
			l_interval = Long.parseLong(interval.substring(0,len-2)) ;
		else if(interval.endsWith("s"))
			l_interval = Long.parseLong(interval.substring(0,len-1))*1000 ;
		else
			l_interval = Long.parseLong(interval)*1000 ;
		//filename=aa page_ele_id= btn_title
		//
		
		if(l_interval<1000)
			throw new JspTagException("page element refresh interval cannot small than 1 second!");
		
		
		JspWriter jw = pageContext.getOut() ;
		
		try
		{
			HashMap<String,String> block2val = new HashMap<String,String>() ;
			block2val.put("uid",uid);
			block2val.put("ele_ids", eleids) ;
			block2val.put("interval", ""+l_interval) ;
			getTemplate().writeOut(jw, block2val) ;
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe);
		}
		
		return EVAL_PAGE;
	}
}

