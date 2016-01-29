package com.dw.web_ui;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.dict.DataNode;

/**
 * 用来指定当前使用的语言文件
 * 
 * @author Jason Zhu
 */
public class WebPageLangFileTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "wb_lang";
	
	

	public static class PageLangBuffer implements Map
	{
		private String lang = null;
		private PageContext pageC = null ;
		
		private WebPageLang pageLang = null;

		private Stack<WebPageLang> plStack = new Stack<WebPageLang>();

		PageLangBuffer(PageContext pc,WebPageLang ppl)
		{
			pageC = pc ;
			lang = AppConfig.getAppLang(pc);
			pageLang = ppl;
			// plStack.push(ppl);
		}

		public WebPageLang getCurPageLang()
		{
			if (plStack.size() > 0)
				return plStack.peek();

			// 返回全局
			return pageLang;
		}
		
		public String getLangValue(String key)
		{
			return getLangValue(key, lang) ;
		}
		
		private String getLangValue(String key, String lang)
		{
			int s = plStack.size() ;
			DataNode dn = null ;
			for(int i = s -1 ; i >=0 ; i --)
			{
				WebPageLang wpl = plStack.elementAt(i);
				dn = wpl.getLangDataNode(key);
				if(dn!=null)
					break ;
			}
			
			if(dn==null&&pageLang!=null)
			{
				dn = pageLang.getLangDataNode(key);
			}
			
			if (dn == null)
				return "[X]" + key + "[X]";

			String tmps = dn.getNameByLang(lang);
			if (tmps != null)
				return tmps;
			return "[X]" + key + "[X]";
		}

		public int size()
		{
			return 0;
		}

		public boolean isEmpty()
		{
			return false;
		}

		public boolean containsKey(Object key)
		{
			return false;
		}

		public boolean containsValue(Object value)
		{
			return false;
		}

		public Object get(Object key)
		{
			return getLangValue((String)key);
		}

		public Object put(Object key, Object value)
		{
			return null;
		}

		public Object remove(Object key)
		{
			return null;
		}

		public void putAll(Map t)
		{
			
		}

		public void clear()
		{
			
		}

		public Set keySet()
		{
			return null;
		}

		public Collection values()
		{
			return null;
		}

		public Set entrySet()
		{
			return null;
		}
	}
	
	private static class IncServletOutputStream extends ServletOutputStream
	{
		ByteArrayOutputStream baos = null;//new ByteArrayOutputStream() ;
		
		public IncServletOutputStream()
		{
			baos = new ByteArrayOutputStream() ;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			baos.write(b);
		}
		
		
	}
	
	private static class IncResponse extends HttpServletResponseWrapper
	{
		String encod = null ;
		IncServletOutputStream outputs = null ;
		//BufferedOutputStream bos = new BufferedOutputStream() ;
		public IncResponse(HttpServletResponse r,String enc)
		{
			super(r);
			encod = enc ;
			
			outputs = new IncServletOutputStream() ;
		}
		
		public String getCharacterEncoding()
		{
			return encod ;
		}
		
		public ServletOutputStream 	getOutputStream()
		{
			return outputs ;
		}
		
		public byte[] getCont()
		{
			return outputs.baos.toByteArray();
		}
	}

	static HashMap<String, Object> path2lang = new HashMap<String, Object>();

	static WebPageLang getPageLang(PageContext pc, String path,boolean brefresh)
			throws JspTagException
	{
		WebPageLang wpl = null;
		
		if(!(AppConfig.isDebug()&&brefresh))
		{
			Object o = path2lang.get(path);
			if(o!=null)
			{
				if(o instanceof WebPageLang)
					return (WebPageLang)o;
				else
					return null ;
			}
		}
		
		try
		{
			RequestDispatcher rd = pc.getRequest().getRequestDispatcher(path+".xml");
			//if(rd==null)
			//	rd = pc.getRequest().getRequestDispatcher(path+".xml");
			
			if(rd!=null)
			{
				IncResponse resp = new IncResponse((HttpServletResponse)pc.getResponse(),"UTF-8") ;
				rd.include(pc.getRequest(), resp);
				byte[] cont = resp.getCont() ;
				if(cont!=null&&cont.length>0)
				{
					try
					{
						wpl = new WebPageLang(cont, null);
					}
					catch(Exception ee)
					{
						ee.printStackTrace();
					}
				}
			}
			else
			{
				String realpath = AppConfig.getSoleWebAppRoot()+path ;
				
				//String realpath = pc.getServletContext().getRealPath(path);
				File f = new File(realpath+".xml");
				if (!f.exists())
					f = new File(realpath);
				
				if (f.exists())
					wpl = new WebPageLang(f, null);
			}
			
			if(wpl==null)
				path2lang.put(path, "");
			else
				path2lang.put(path, wpl);
			
			return wpl;
		}
		catch (Exception e)
		{
			throw new JspTagException(e);
		}
	}

	static PageLangBuffer getOrCreatePageLangBuffer(PageContext pc)
			throws JspTagException
	{
		PageLangBuffer plb = (PageLangBuffer) pc.getAttribute(CXT_ATTRN);
		if (plb != null)
			return plb;

		String path = ((HttpServletRequest) pc.getRequest()).getServletPath()+".lang";

		try
		{
			//File langf = new File(f.getAbsolutePath() + ".lang");
			boolean brefresh = "true".equalsIgnoreCase(pc.getRequest().getParameter("lang_refresh")) ;
			WebPageLang wpl = getPageLang(pc, path,brefresh);
			plb = new PageLangBuffer(pc,wpl);
			pc.setAttribute(CXT_ATTRN,plb);
			return plb;
		}
		catch (Exception e)
		{
			throw new JspTagException(e);
		}
	}

	private String path = null;

	public void setPath(String p)
	{
		path = p;
	}

	public int doStartTag() throws JspTagException
	{
		PageLangBuffer plb = getOrCreatePageLangBuffer(pageContext);
		boolean brefresh = "true".equalsIgnoreCase(pageContext.getRequest().getParameter("lang_refresh")) ;
		WebPageLang wpl = getPageLang(pageContext, path,brefresh);
		if(wpl==null)
			throw new RuntimeException("Cannot find page lang file=" + path);
		plb.plStack.push(wpl);
		return EVAL_BODY_INCLUDE;

	}

	public int doEndTag() throws JspTagException
	{
		PageLangBuffer plb = getOrCreatePageLangBuffer(pageContext);
		plb.plStack.pop();
		return EVAL_PAGE;
	}
}