package com.dw.web_ui.editor;

import java.io.File;
import java.io.*;
import java.util.HashMap;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageTemplate;

/**
 * ��һЩӦ�ó���,������Ҫʵ��һ�����Ƶ�Word�༭,Ԥ����ӡ����
 * ͬʱ����Ȳ���Ҳ�ܵ�һ
 * 
 * �ı�ǩ���������������������һ����ǩ
 * 
 * ֧��:
 * 	1,�ĵ��ı༭
 *	2,ͨ��ָ����jsp ·����Ϊ���������Զ�����--��jspʵ��ʱ����form�ύ��Ĵ���
 *		��ǩ��֧����չ�������ύ.��������ʹ�ĳ����ı���ʽ����֮��,Ϊ����jsp�ύ������Ҫ�Ĳ���,��id��
 *		�ύʱ����ajax��ʽ,ҳ�治��ˢ��
 *	3,֧��ģ�弰ģ�����--����������ʾ֮��ʹ��
 *
 * 
 * @author Jason Zhu
 */
public class SmartEditorTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_HTML_EDITOR2";
	
	static WebPageTemplate templ = null ;//new ArrayList<String>() ;
	
	static
	{
		
	}
	
	private static HashMap<String, WebPageTemplate> name2wpt = new HashMap<String, WebPageTemplate>();
	
	private static WebPageTemplate getPageTemplateByPath(PageContext pc,String p)
	{
		WebPageTemplate wpt = name2wpt.get(p);
		if (wpt != null)
			return wpt;

		synchronized (name2wpt)
		{
			wpt = name2wpt.get(p);
			if (wpt != null)
				return wpt;

			String pagetxt = AppConfig.readWebPageTxtByPath(pc,p,"UTF-8") ;
			if(pagetxt==null)
				pagetxt = "" ;
			wpt = new WebPageTemplate(p, pagetxt) ;
			
			name2wpt.put(p, wpt);
			return wpt;
		}
	}
	
	private static WebPageTemplate getTemplate() throws IOException
	{
		if(templ!=null)
		{
			return templ;
		}
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/editor/SmartEditor.htm")) ;
		return templ ;
	}
	
	private String id_name = null ;
	
	private boolean bInsertImg = false;
	private boolean bEmotion = false;
	private int height = 20 ;
	private String rows = "20" ;
	private String lang = "zh";
	
	private String submitUrl = null ;
	private String submitTitle = "Save" ;
	
	private String template = null ;
	
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
	
	public void setSubmit_url(String su)
	{
		submitUrl = su ;
	}
	
	public void setSubmit_title(String st)
	{
		submitTitle = st ;
	}
	
	public void setTemplate(String t)
	{
		template = t ;
	}
	
	
	HashMap<String,String> blockname2cont = new HashMap<String,String>() ;
	HashMap<String,String> paramname2val = new HashMap<String,String>() ;
	String mainCont = null ;
	
	void setBlockContent(String blockname,String cont)
	{
		blockname2cont.put(blockname, cont);
	}
	
	void setParamValue(String paramname,String val)
	{
		paramname2val.put(paramname, val) ;
	}
	
	void setMainCont(String mainc)
	{
		mainCont = mainc ;
	}
	/**
	 * ���ģ�����ʼ����
	 */
	public int doStartTag() throws JspTagException
	{
		//clear old block cont
		blockname2cont = new HashMap<String,String>() ;
		paramname2val = new HashMap<String,String>() ;
		mainCont = null ;
		
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
				jw.write("<script src=\"/system/ui/main.js\"></script>");
				jw.write("<script src=\"/system/ui/ajax.js\"></script>");
				
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
	 * �������Plain�ı�ת����html�ı�,���ʺ�ҳ�����չʾ����Ҫ
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
	
	
	private String getHtmlCont() throws IOException
	{
		if(Convert.isNotNullEmpty(template))
		{//do template doing
			WebPageTemplate wpt = getPageTemplateByPath(pageContext,template) ;
			if(wpt==null)
				throw new IllegalArgumentException("cannot load template="+template);
			
			StringWriter sw = new StringWriter();
			wpt.writeOut(sw, blockname2cont);
			return sw.toString();
		}
		else
		{
			if(mainCont!=null)
				return mainCont ;
			
			String htmltxt = null ;
			if(this.bodyContent!=null)
				htmltxt = bodyContent.getString() ;
			if(htmltxt==null)
				htmltxt = "" ;
			
			return htmltxt ;
		}
	}
	
	public int doEndTag() throws JspTagException
	{
		try
		{
			JspWriter jw = pageContext.getOut() ;
			String htmltxt = getHtmlCont() ;
			
			String t = UUID.randomUUID().toString().replaceAll("-", "");
			String nid = "p"+t ;
			String qid = "q"+t;
			String edit_id = "edit_"+t ;
			String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			
			StringBuilder toolbarc = new StringBuilder() ;
			if(Convert.isNotNullEmpty(submitUrl))
			{
				toolbarc.append("[item]\r\n") ;
				toolbarc.append("title="+submitTitle+"\r\n") ;
				toolbarc.append("url=javascript:submit_"+edit_id+"()\r\n") ;
				toolbarc.append("textview=1\r\n") ;
				//jw.write("img=images/i.p.send.gif
			}
			toolbarc.append("[item]\r\n") ;
			toolbarc.append("title=��ӡ\r\n") ;
			toolbarc.append("url=javascript:print_"+edit_id+"()\r\n") ;
			toolbarc.append("textview=1\r\n") ;
			
			//
			
			HashMap<String,String> block2val = new HashMap<String,String>() ;
			block2val.put("qid",qid);
			block2val.put("nid", nid) ;
			block2val.put("uid", t) ;
			block2val.put("form_id", "form_"+t) ;
			
			block2val.put("edit_id", edit_id) ;
			block2val.put("toolbar_cont", toolbarc.toString()) ;
			
			if(Convert.isNotNullEmpty(submitUrl))
				block2val.put("submit_url",this.submitUrl) ;
			else
				block2val.put("submit_url","") ;
			
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
			
			StringBuilder hiddeninputsb = new StringBuilder() ;
			for(Map.Entry<String, String> p2v:paramname2val.entrySet())
			{
				hiddeninputsb.append("<input type='hidden' name='")
					.append(p2v.getKey()).append("' value='").append(p2v.getValue())
					.append("'/>\r\n") ;
			}
			
			block2val.put("ext_inputs",hiddeninputsb.toString());
			
			getTemplate().writeOut(jw, block2val) ;
			
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}