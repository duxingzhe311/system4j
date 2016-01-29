package com.dw.web_ui.print;

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

public class PrintButtonTag extends BodyTagSupport
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
	
	private String printEleId = "" ;
	
	public void setPrint_ele_id(String pei)
	{
		printEleId = pei ;
		if(printEleId==null)
			printEleId = "" ;
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
		
		String hiddenids = "" ;
		String title = "Print" ;
		String edit_title = "Edit Print" ;
		String printeleid = "" ;
		String edit_dis = "" ;
		if(exp_info!=null)
		{
			HashMap<String,String> ps = Convert.transPropStrToMap(exp_info) ;
			String ids = ps.get("hidden_ids") ;
			StringBuilder sb = new StringBuilder();
			if(Convert.isNotNullEmpty(ids))
			{
				StringTokenizer st = new StringTokenizer(ids,",|") ;
				if(st.hasMoreTokens())
				{
					sb.append('\'').append(st.nextToken()).append('\'') ;
				}
				
				while(st.hasMoreTokens())
				{
					sb.append(",\'").append(st.nextToken()).append('\'') ;
				}
			}
			
			edit_dis = ps.get("edit_dis") ;
			if(edit_dis==null)
				edit_dis = "" ;
			else if("false".equalsIgnoreCase(edit_dis))
				edit_dis = "none" ;
			
			hiddenids = sb.toString() ;
			
			title = ps.get("title") ;
			edit_title = ps.get("edit_title") ;
			if(Convert.isNullOrEmpty(title))
				title = "Print" ;
			if(Convert.isNullOrEmpty(edit_title))
				edit_title = "Edit Print" ;
			
			printeleid = ps.get("print_ele_id") ;
		}
		
		//filename=aa page_ele_id= btn_title
		//
		
		
		JspWriter jw = pageContext.getOut() ;
		
		try
		{
			HashMap<String,String> block2val = new HashMap<String,String>() ;
			block2val.put("uid",uid);
			block2val.put("hidden_ids",hiddenids);
			block2val.put("btntitle",title);
			block2val.put("edit_dis",edit_dis);
			block2val.put("edit_btntitle",edit_title);
			if(printeleid!=null)
				block2val.put("openerid",printeleid);
			else
				block2val.put("openerid",printEleId);
			
			
			getTemplate().writeOut(jw, block2val) ;
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe);
		}
		
		return EVAL_PAGE;
	}
}

