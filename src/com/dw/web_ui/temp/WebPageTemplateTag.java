package com.dw.web_ui.temp;

import java.io.*;
import java.util.*;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.AppConfig;
import com.dw.web_ui.WebRes;

/**
 * 支持页面输出模板的标签
 * 
 * 比如一个项目如果使用统一的界面风格,就有可能定制页面模板
 * 
 * 每个页面模板使用的是文本文件,里面包含插入位置形如:[#blockname#] 每个插入位置用blockname进行标记
 * 
 * 在使用时,tag内包含WebPageChildTag ,每个tag都需要指定使用哪个block 并且ChildTag必须按照顺序写入.
 * 
 * @author Jason Zhu
 */
public class WebPageTemplateTag extends BodyTagSupport
{

	private static class SegPos
	{
		int segPos = -1;
	}

	public static String CXT_ATTRN = "_WEB_PAGE_LANG";

	private static HashMap<String, WebPageTemplate> name2wpt = new HashMap<String, WebPageTemplate>();

	static
	{

	}

	// public static void renderPageTemplate(String p)

	private static WebPageTemplate getPageTemplateByPath(String p, String enc,
			String cxt_root)
	{
		WebPageTemplate wpt = name2wpt.get(p);
		if (wpt != null)
			return wpt;

		synchronized (name2wpt)
		{
			wpt = name2wpt.get(p);
			if (wpt != null)
				return wpt;

			wpt = loadTemplate(p, enc, cxt_root);
			if (wpt == null)
				throw new RuntimeException("cannot load template file=" + p);

			name2wpt.put(p, wpt);
			return wpt;
		}
	}

	private static WebPageTemplate loadTemplate(String p, String enc,
			String cxt_root)
	{
		File f = new File(p);

		if (!f.exists())
			return null;

		FileInputStream fis = null;
		if (enc == null || enc.equals(fis))
			enc = "UTF-8";

		try
		{
			fis = new FileInputStream(f);
			byte[] buf = new byte[(int) f.length()];
			fis.read(buf);
			String s = new String(buf, enc);
			s = s.replaceAll("\\[\\$CXT_ROOT\\]", cxt_root);

			return new WebPageTemplate(p, s);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if (fis != null)
					fis.close();
			}
			catch (IOException ioe)
			{
			}
		}
	}

	// 模板的定义路径,他是jsp页面中
	// private String defPath = null ;
	// 模板文件在文件系统中的路径
	private String tempPath = null;

	// 上下文路径,用来替代模板中的引用路径的绝对化处理
	private String cxtRoot = "/";

	private String encodStr = null;

	private transient WebPageTemplate pageTemp = null;

	private transient int segPos = -1;

	private WebPageTemplate getTemplate()
	{
		if (pageTemp != null)
			return pageTemp;

		pageTemp = getPageTemplateByPath(tempPath, encodStr, cxtRoot);
		return pageTemp;
	}

	/**
	 * 设置模板的路径,可以是相对路径/或webapp的绝对路径
	 * 
	 * 系统首先查找本地webapp内容,如果存在对应的模板文件,则确定 如果本地不存在,则判定是否是tomato模式,如果是,则从全局上进行查找
	 * 
	 * @param p
	 * @throws IOException
	 */
	public void setTemplate(String p) throws IOException
	{
		File realp = null;
		// 尝试通过本webapp上下文获取绝对路径
		HttpServletRequest hsr = (HttpServletRequest) this.pageContext
				.getRequest();

		String tmpp = this.pageContext.getServletContext().getRealPath(p);
		File f = new File(tmpp);
		if (f.exists())
		{
			realp = f;
			cxtRoot = WebRes
					.getContextRootPath((HttpServletRequest) this.pageContext
							.getRequest());
		}

		if (realp == null && !AppConfig.isSole() && p.startsWith("/"))
		{// tomato方式下,通过全局运行
			tmpp = AppConfig.getTomatoWebappBase();
			File tmpf = new File(tmpp, p);
			if (tmpf.exists())
			{
				realp = tmpf;
				// int k = p.indexOf('/',1) ;
				// if(k>0)
				// cxtRoot = p.substring(0,k+1);
				cxtRoot = "/";
			}
		}

		if (realp == null)
			throw new RuntimeException("no template found with path input=" + p);

		tempPath = realp.getCanonicalPath();
	}

	public void setEncoding(String encod)
	{
		encodStr = encod;
	}

	// private SegPos getCurSegPos()
	// {
	//		
	// }

	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		// this.pageContext.setAttribute("", getTemplate())
		segPos++;
		return BodyTag.EVAL_BODY_INCLUDE;
	}

	/**
	 * 被WebPageValueTag和WebPageChildTag调用的方法
	 * 
	 * 当运行到Value或Child方法时,需要把模板中的前面还没有输出的内容进行输出
	 * 
	 * @param blockname
	 * @throws IOException
	 */
	JspWriter beforeRenderBlock(String blockname) throws IOException
	{
		// 从segPos开始,寻找对应的block,如果不是则立刻输出模板内容
		WebPageTemplate wpt = getTemplate();
		ArrayList als = wpt.getContList();
		int s = als.size();
		
		
		Object o = als.get(segPos);
		if (o instanceof WebPageBlock)
		{
			WebPageBlock bi = (WebPageBlock) o;
			if (bi.name.equals(blockname))
			{
				//如果当前的块就是准备输入的块,则--该判断用来支持同一个ChildTag可以在循环中运行
				return this.pageContext.getOut();
			}
		}
		
		int j = -1;
		for (int i = segPos; i < s; i++)
		{
			o = als.get(i);
			if (o instanceof WebPageBlock)
			{
				WebPageBlock bi = (WebPageBlock) o;
				if (bi.name.equals(blockname))
				{
					j = i;
					break;
				}
			}
		}

		if (j < 0)
			throw new RuntimeException("cannot find block =[" + blockname
					+ "],may be it is not exist or already be render out");

		// 输出对应block块之前的内容
		while (segPos < j)
		{
			o = als.get(segPos);
			if (o instanceof String)
			{
				this.pageContext.getOut().write((String) o);
			}
			segPos++;
		}

		segPos = j ;
		return this.pageContext.getOut();
	}

	// public void setBodyContent(BodyContent bodyContent)
	// {
	// System.out.println("setBodyContent...");
	// this.bodyContent = bodyContent;
	// }
	//
	// public void doInitBody() throws JspTagException
	// {
	// System.out.println("doInitBody....");
	// }

	// public int doAfterBody() throws JspTagException
	// {
	// return SKIP_BODY;
	// }

	public int doEndTag() throws JspTagException
	{
		// 输出还没有输出的模板剩余内容
		try
		{
			WebPageTemplate wpt = getTemplate();
			ArrayList als = wpt.getContList();
			int s = als.size();
			while (segPos < s)
			{
				Object o = als.get(segPos);
				if (o instanceof String)
					this.pageContext.getOut().write((String) o);

				segPos++;
			}
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}

		segPos = -1;
		return EVAL_PAGE;
	}
}