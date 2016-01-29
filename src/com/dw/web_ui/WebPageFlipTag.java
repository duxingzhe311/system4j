package com.dw.web_ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.system.gdb.GDBPageAccess;
import com.dw.web_ui.temp.WebPageTemplate;

/**
 * 翻页支持控件
 * @author Jason Zhu
 */
public class WebPageFlipTag extends BodyTagSupport
{
	private static final String FLIP_ATTR = "__flip_attr_style" ;
//	private static WebPageTemplate templ = null ;
	
	String xormObjListVar = null ;
	
	public void setXorm_objlist_var(String xov)
	{
		xormObjListVar = xov ;
	}
//	
//	private static WebPageTemplate getTemplate() throws IOException
//	{
//		if(templ!=null)
//		{
//			return templ;
//		}
//		
//		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/print/HtmlPrint.htm")) ;
//		return templ ;
//	}
	
	private void writePage(JspWriter jw)
	throws IOException
{
	if(Convert.isNullOrEmpty(xormObjListVar))
		return ;
	
	Object tmpo = this.pageContext.getAttribute(xormObjListVar) ;
	if(!(tmpo instanceof GDBPageAccess))
		return ;
	
	GDBPageAccess pa = (GDBPageAccess)tmpo;
	//int total = pa.getListObjTotalNum() ;
	int curp = pa.getPageCur() ;
	//int pagen = pa.getPageTotalNum() ;
	jw.println("<div class='pages'><table height='10' style='border: 0;margin: 0' cellpadding='0' cellspacing='0'><tr  align='right'><td width='99%'>&nbsp;</td><td nowrap='nowrap'>") ;
	
	HttpServletRequest req = (HttpServletRequest)pageContext.getRequest() ;
	String u = req.getRequestURI() ;
	int p = u.lastIndexOf('/') ;
	if(p>=0)
		u = u.substring(p+1) ;
	String qs = req.getQueryString() ;
	if(qs!=null)
	{
		StringTokenizer st = new StringTokenizer(qs,"&") ;
		StringBuilder sb = new StringBuilder() ;
		while(st.hasMoreTokens())
		{
			String ss = st.nextToken() ;
			if(ss.startsWith(GDBPageAccess.PN_PAGE_CUR))
				continue ;
			
			sb.append(ss).append('&') ;
		}
		qs = sb.toString() ;
	}
	else
	{
		qs = "" ;
	}
	
	jw.print("<span style='font-size: 10pt'>当前页"+(curp+1)+"&nbsp;&nbsp;</span>") ;
	
	if(curp>0)
	{
		jw.println("<a href='"+u+"?"+qs+GDBPageAccess.PN_PAGE_CUR+"="+(curp-1)+"' style='font-weight: bold;'>上一页</a>");
	}
	jw.println("<a href='"+u+"?"+qs+GDBPageAccess.PN_PAGE_CUR+"="+(curp+1)+"' style='font-weight: bold;'>下一页</a>");
	
	/*
	jw.println("<a href='thread.php?fid-136-search--page-1.html' style='font-weight: bold;'>&lt;&lt;</a>");
	
	jw.println("<b> 2 </b>");
	jw.println("<a href='thread.php?fid-136-search--page-3.html'>3</a>");
	jw.println("<a href='thread.php?fid-136-search--page-4.html'>4</a>");
	jw.println("<a href='thread.php?fid-136-search--page-5.html'>5</a>");
	jw.println("<a href='thread.php?fid-136-search--page-6.html'>6</a>");
	jw.println("<input size='3' onkeydown='javascript: if(event.keyCode==13){ location='thread.php?fid=136&search=&page='+this.value+'';return false;}' type='text'>");
	jw.println("<a href='thread.php?fid-136-search--page-53.html' style='font-weight: bold;'>&gt;&gt;</a>");
	jw.println(" 第 "+(pa.getPageCur()+1)+" 页 共"+pagen+" 页");
	*/
	jw.println("</td></tr></table></div>") ;
}
	
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(FLIP_ATTR)))
		{
			JspWriter jw = pageContext.getOut() ;
			
			try
			{
				jw.write("<style>");
				jw.write(".pages{margin:3px 0;font:11px/12px Tahoma}") ;
				jw.write(".pages *{vertical-align:middle;}") ;
				jw.write(".pages a{padding:1px 4px 1px;border:1px solid #353535;margin:0 1px 0 0;text-align:center;text-decoration:none;font:normal 12px/14px verdana;}") ;
				jw.write(".pages a:hover{border:#353535 1px solid;background:#666;text-decoration:none;color:#FFF}") ;
				jw.write(".pages input{margin-bottom:0px;border:1px solid #353535;height:15px;font:bold 12px/15px Verdana;padding-bottom:1px;padding-left:1px;margin-right:1px;color:#353535;}") ;
				jw.write("</style>");
			}
			catch(IOException ioe)
			{}
			
			pageContext.setAttribute(FLIP_ATTR,"true") ;
		}
		//AjaxLazyShower(buffer_len)
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspTagException
	{
		
		String exp_info = null ;
		if(bodyContent!=null)
			exp_info = bodyContent.getString() ;
		
		int cur_p = 0;
		int total_num = -1 ;
		int total_pagenum = 0 ;
		int pagesize = 20;
		String pageurl = "" ;
		
		
		if(exp_info!=null)
		{
			HashMap<String,String> ps = Convert.transPropStrToMap(exp_info) ;
			cur_p = Convert.parseToInt32(ps.get("cur_page"),0) ;
			pagesize = Convert.parseToInt32(ps.get("page_size"),20) ;
			total_num = Convert.parseToInt32(ps.get("total"),-1) ;
			total_pagenum = total_num / pagesize + (total_num%pagesize)>0?1:0 ;
			//aa.jsp?cur_page=[page_num]&size=[page_size]
			pageurl = ps.get("page_url_pattern") ;
			if(pageurl==null)
				pageurl = "" ;
		}
		
		//filename=aa page_ele_id= btn_title
		//
		
		
		JspWriter jw = pageContext.getOut() ;
		
		try
		{
			if(cur_p>0)
			{
				jw.write("<a href='"+pageurl+"&pagesize="+pagesize+"&curpage="+(cur_p-1));
				jw.write("'>上一页</a>&nbsp;&nbsp;");
			}
			jw.println("<div class='pages'>") ;
			for(int k = 0 ; k < 6 && k < total_pagenum ; k ++)
			{
				if(k==0)
				{
					jw.println("<a href='thread.php?fid-136-search--page-1.html' style='font-weight: bold;'>&lt;&lt;</a>");
				}
				else
				{
					if(cur_p==k)
						jw.println("<b> +(k+1)+ </b>");
					else
						jw.println("<a href='"+pageurl+"'>"+(k+1)+"</a>");
				}
			}
			jw.println("<a href='thread.php?fid-136-search--page-1.html' style='font-weight: bold;'>&lt;&lt;</a>");
			
			jw.println("<b> 2 </b>");
			jw.println("<a href='thread.php?fid-136-search--page-3.html'>3</a>");
			jw.println("<a href='thread.php?fid-136-search--page-4.html'>4</a>");
			jw.println("<a href='thread.php?fid-136-search--page-5.html'>5</a>");
			jw.println("<a href='thread.php?fid-136-search--page-6.html'>6</a>");
			jw.println("<input size='3' onkeydown='javascript: if(event.keyCode==13){ location='thread.php?fid=136&search=&page='+this.value+'';return false;}' type='text'>");
			jw.println("<a href='thread.php?fid-136-search--page-53.html' style='font-weight: bold;'>&gt;&gt;</a>");
			//jw.println(" Pages: ( 2/53 total )");
			jw.println("</div>") ;
			
//			jw.write("<a href='"+pageurl+"&pagesize="+pagesize+"&curpage="+(cur_p+1));
//			jw.write("'>下一页</a>");
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe);
		}
		
		return EVAL_PAGE;
	}
}

