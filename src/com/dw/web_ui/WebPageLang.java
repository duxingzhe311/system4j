package com.dw.web_ui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.dw.system.dict.DataClass;
import com.dw.system.dict.DataNode;
import com.dw.system.dict.DictManager;

/**
 * ֧��jspҳ������Ե���
 * 
 * ��ÿ����Ҫʵ�ֶ�����֧�ֵ�jspҳ���ļ�ͬһ��Ŀ¼�£�����Զ������Ե����������ļ�
 * 
 * ���磺ҳ���ļ� show_xxx.jsp �����ļ���Ϊ show_xxx.jsp.lang
 * 
 * ����,.lang�ļ����ֵ��ļ�
 * 
 * ��ʵ��show_xxx.jsp�еĶ���������ʱ����Ҫ�����µ���
 * 
 * WebPageLang wpl = WebPageLang.getPageLang(this,request) ;
 * wpl.getLangValue("head.title");
 * 
 * TODO jsp Tag ֧��
 * 
 * @author Jason Zhu
 */
public class WebPageLang
{
	static Object locker = new Object();

	/**
	 * ���jsp��Ӧ���࣬�����Զ����ӳ��
	 */
	private static HashMap<Class, WebPageLang> jspClass2Lang = new HashMap<Class, WebPageLang>();

	/**
	 * ��������uri��jspҳ���Ӧ��java��֮���ӳ��
	 */
	private static HashMap<String, Class> reqUri2JspClass = new HashMap<String, Class>();

	public static WebPageLang getPageLang(Servlet jspp, HttpServletRequest req)
	{
		Class jspc = jspp.getClass();
		WebPageLang wpl = (WebPageLang) jspClass2Lang.get(jspc);
		if (wpl != null)
		{
			// System.out.println("get page lang in cache!");
			return wpl;
		}

		synchronized (locker)
		{
			wpl = (WebPageLang) jspClass2Lang.get(jspc);
			if (wpl != null)
				return wpl;

			try
			{
				Class tmpc = reqUri2JspClass.get(req.getRequestURI());
				if (tmpc != null)
				{// ������Ϊҳ����޸ĵ���jspҳ���Ӧ�������ı䣬���ɵ�������Ӧ�����Զ����Ѿ�������
					// ��Ҫ�������ڴ���ɾ��
					System.out.println("remove page lang by old="
							+ req.getRequestURI());
					jspClass2Lang.remove(tmpc);
				}

				// ����װ��������Ϣ
				String realpath = jspp.getServletConfig().getServletContext()
						.getRealPath(req.getServletPath());
				File f = new File(realpath);
				File langf = new File(f.getAbsolutePath() + ".lang");
				// File dirf = f.getParentFile();
				// System.out.println("jsp path="+f.getAbsolutePath());
				// File[] langfs = dirf.listFiles(new
				// LangFilenameFilter(f.getName()));
				// if(langfs==null)
				// wpl = new WebPageLang(langfs) ;
				if (!langf.exists())
					return null;

				// req.getCookies()
				wpl = new WebPageLang(langf, null);

				System.out.println("add new page lang=" + req.getRequestURI());
				reqUri2JspClass.put(req.getRequestURI(), jspc);
				jspClass2Lang.put(jspc, wpl);

				return wpl;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}

	}

	static class LangFilenameFilter implements FilenameFilter
	{
		private String jspFN = null;

		public LangFilenameFilter(String jspfn)
		{
			jspFN = jspfn.toLowerCase();
		}

		public boolean accept(File dir, String name)
		{
			System.out.println("file name=" + name);
			String tmpn = name.toLowerCase();
			if (!tmpn.startsWith(jspFN))
				return false;

			return tmpn.endsWith(".lang");
		}
	}

	String defaultLang = "cn";

	DataClass langDC = null;

	WebPageLang(File langf, String default_lan)
			throws Exception
	{
		langDC = DictManager.loadDataClass(null,langf);
		if (default_lan != null)
			defaultLang = default_lan;
	}

	WebPageLang(byte[] langf, String default_lan)
			throws Exception
	{
		langDC = DictManager.loadDataClass(null,langf);
		if (default_lan != null)
			defaultLang = default_lan;
	}

	public String getLangValue(String key)
	{
		if (langDC == null)
			return "[X]" + key + "[X]";
		DataNode dn = langDC.findDataNodeByName(key);
		if (dn == null)
			return "[X]" + key + "[X]";
		String tmps = dn.getNameByLang(defaultLang);
		if (tmps != null)
			return tmps;

		return "[X]" + key + "[X]";
	}

	public DataNode getLangDataNode(String key)
	{
		if (langDC == null)
			return null;

		return langDC.findDataNodeByName(key);
	}

	public String getLangValue(String key, String lang)
	{
		if (langDC == null)
			return "[X]" + key + "[X]";

		if (lang == null)
		{// ʹ��ҳ�涨�������
			lang = defaultLang;
		}

		DataNode dn = langDC.findDataNodeByName(key);
		if (dn == null)
			return "[X]" + key + "[X]";
		String tmps = dn.getNameByLang(lang);
		if (tmps != null)
			return tmps;
		return "[X]" + key + "[X]";
	}
}
