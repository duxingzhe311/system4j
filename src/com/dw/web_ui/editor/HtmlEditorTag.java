package com.dw.web_ui.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageTemplate;

public class HtmlEditorTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_HTML_EDITOR";
	
	static WebPageTemplate templ = null ;//new ArrayList<String>() ;
	
	static
	{
		
	}
	
	
	private static WebPageTemplate getTemplate() throws IOException
	{
		if(templ!=null)
		{
			return templ;
		}
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/editor/HtmlEditor.htm")) ;
		return templ ;
	}
	
	private String id_name = null ;
	
	private boolean bInsertImg = false;
	private boolean bEmotion = false;
	private int height = 20 ;
	
	public void setId_name(String idn)
	{
		id_name = idn ;
	}
	
	public void setHeight(String h)
	{
		if(h==null||h.equals(""))
			return ;
		
		height = Integer.parseInt(h);
	}
	
	public void setInsert_img(String ii)
	{
		bInsertImg = "true".equalsIgnoreCase(ii);
	}
	
	public void setEmotion(String em)
	{
		bEmotion = "true".equals(em);
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
				JspWriter jw = pageContext.getOut() ;
				jw.write("<script type='text/javascript' src='/system/ui/js/MyWebEditor.js' ></script>");
				jw.write("<script language='javascript'>var MyWebEditor_Path = '/system/ui/';</script>");

				jw.write("<link href='/system/ui/js/MyWebEditor.css' rel='stylesheet' type='text/css'  media='screen'/>");
				
				pageContext.setAttribute(CXT_ATTRN, "true");
			}
			catch(IOException ioe)
			{
				throw new JspTagException(ioe);
			}
		}
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	/**
	 * 对输入的Plain文本转换成html文本,以适合页面输出展示的需要
	 * @param input
	 * @return
	 */
	static String plainToHtml(String input)
	{
		if (input == null)
		{
			return "";
		}

		char[] array = input.toCharArray();

		StringBuffer buf = new StringBuffer(array.length + array.length / 2);

		for (int i = 0; i < array.length; i++)
		{
//			if ( (i != 0) && (i % 60 == 0))
//			{
//				buf.append("<br/>");
//			}
			
			switch (array[i])
			{
				case '<':
					buf.append("&lt;");
					break;
				case '>':
					buf.append("&gt;");
					break;
				case '&':
					buf.append("&amp;");
					break;
				case '\"':
					buf.append("&#34;");
					break;
				case '\'':
					buf.append("&#39;");
					break;
				default:
					buf.append(array[i]);
					break;
			}
		}
		return buf.toString();
	}
	
	public int doEndTag() throws JspTagException
	{
		try
		{
			JspWriter jw = pageContext.getOut() ;
			String htmltxt = this.bodyContent.getString() ;
			//String t = UUID.randomUUID().toString().replaceAll("-", "");
			//String nid = "p"+t ;
			//String qid = "q"+t;
			//String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			
			HashMap<String,String> block2val = new HashMap<String,String>() ;
			block2val.put("id_name",id_name);
			block2val.put("frame_id",id_name+"_f");
			block2val.put("toolbar",id_name+"_t");
			block2val.put("txt_val",plainToHtml(htmltxt));
			
			if(!this.bEmotion)
				block2val.put("emotion_style","display:none");
			
			if(!this.bInsertImg)
				block2val.put("insertimg_style","display:none");
			
			getTemplate().writeOut(jw, block2val) ;
			
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}
