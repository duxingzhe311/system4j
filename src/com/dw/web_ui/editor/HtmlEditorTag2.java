package com.dw.web_ui.editor;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageTemplate;

/**
 * 基于Xinha控件实现
 * @author Jason Zhu
 *
 */
public class HtmlEditorTag2 extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_HTML_EDITOR2";
	
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
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/editor/HtmlEditor2.htm")) ;
		return templ ;
	}
	
	private String id_name = null ;
	
	private boolean bInsertImg = false;
	private boolean bEmotion = false;
	private int height = 20 ;
	private String rows = "20" ;
	private String lang = "zh";
	
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
	
	public void setLang(String lan)
	{
		lang = lan ;
		if(Convert.isNullOrTrimEmpty(lan))
			lang = "zh";
	}
	
	public void setRows(String r)
	{
		rows = r ;
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
				
				jw.write("<script type='text/javascript'>\r\n") ;
				jw.write("_editor_url  = '/_res/xinha/';") ;
				
				jw.write("_editor_lang = '"+lang+"';");      // And the language we need to use in the editor.
				jw.write("_editor_skin = 'blue-look';") ;   // If you want use skin, add the name here
				jw.write("</script>") ;
				jw.write("<script src='/_res/xinha/XinhaCore.js'></script>");
				//jw.write("<script language='javascript'>var MyWebEditor_Path = '/system/ui/';</script>");

				//jw.write("<link href='/system/ui/js/MyWebEditor.css' rel='stylesheet' type='text/css'  media='screen'/>");
				
				pageContext.setAttribute(CXT_ATTRN, "true");
			}
			catch(Exception ioe)
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
			
			HashMap<String,String> block2val = new HashMap<String,String>() ;
			block2val.put("id_name",id_name);
			block2val.put("rows", rows) ;
			if(Convert.isNullOrTrimEmpty(htmltxt))
			{
				//block2val.put("txt_val",plainToHtml("<font style=\"font-family:tahoma;font-size:10pt;\"><p><br/>&nbsp;</p></font>"));
			}
			else
			{
				block2val.put("txt_val",plainToHtml(htmltxt));
			}
				//block2val.put("txt_val",htmltxt);
			
			//block2val.put("toolbar",id_name+"_t");
			
			
			//if(!this.bEmotion)
			//	block2val.put("emotion_style","display:none");
			
			//if(!this.bInsertImg)
			//	block2val.put("insertimg_style","display:none");
			
			getTemplate().writeOut(jw, block2val) ;
			
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}