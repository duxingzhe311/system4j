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
 * ֧��ҳ�����ģ��ı�ǩ
 * 
 * ����һ����Ŀ���ʹ��ͳһ�Ľ�����,���п��ܶ���ҳ��ģ��
 * 
 * ÿ��ҳ��ģ��ʹ�õ����ı��ļ�,�����������λ������:[#blockname#] ÿ������λ����blockname���б��
 * 
 * ��ʹ��ʱ,tag�ڰ���WebPageChildTag ,ÿ��tag����Ҫָ��ʹ���ĸ�block ����ChildTag���밴��˳��д��.
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

	// ģ��Ķ���·��,����jspҳ����
	// private String defPath = null ;
	// ģ���ļ����ļ�ϵͳ�е�·��
	private String tempPath = null;

	// ������·��,�������ģ���е�����·���ľ��Ի�����
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
	 * ����ģ���·��,���������·��/��webapp�ľ���·��
	 * 
	 * ϵͳ���Ȳ��ұ���webapp����,������ڶ�Ӧ��ģ���ļ�,��ȷ�� ������ز�����,���ж��Ƿ���tomatoģʽ,�����,���ȫ���Ͻ��в���
	 * 
	 * @param p
	 * @throws IOException
	 */
	public void setTemplate(String p) throws IOException
	{
		File realp = null;
		// ����ͨ����webapp�����Ļ�ȡ����·��
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
		{// tomato��ʽ��,ͨ��ȫ������
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
	 * ���ģ�����ʼ����
	 */
	public int doStartTag() throws JspTagException
	{
		// this.pageContext.setAttribute("", getTemplate())
		segPos++;
		return BodyTag.EVAL_BODY_INCLUDE;
	}

	/**
	 * ��WebPageValueTag��WebPageChildTag���õķ���
	 * 
	 * �����е�Value��Child����ʱ,��Ҫ��ģ���е�ǰ�滹û����������ݽ������
	 * 
	 * @param blockname
	 * @throws IOException
	 */
	JspWriter beforeRenderBlock(String blockname) throws IOException
	{
		// ��segPos��ʼ,Ѱ�Ҷ�Ӧ��block,����������������ģ������
		WebPageTemplate wpt = getTemplate();
		ArrayList als = wpt.getContList();
		int s = als.size();
		
		
		Object o = als.get(segPos);
		if (o instanceof WebPageBlock)
		{
			WebPageBlock bi = (WebPageBlock) o;
			if (bi.name.equals(blockname))
			{
				//�����ǰ�Ŀ����׼������Ŀ�,��--���ж�����֧��ͬһ��ChildTag������ѭ��������
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

		// �����Ӧblock��֮ǰ������
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
		// �����û�������ģ��ʣ������
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