package com.dw.web_ui;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dw.system.gdb.GDB;
import com.dw.system.util.Mime;

/**
 * ͨ����Դ·���ķ�ʽ�����Դ���ݡ���servlet����֧�ֻ�� ��Դ�ļ����ݣ��繫��ͼƬ��.��ЩͼƬ�������jar�ļ��л��class��ͬһ��Ŀ¼��
 * 
 * ����������init����,�����������˵�ǰwebapp��ClassLoader,����,WebRes��Ѱ����Դʱ��class loader
 * ����һ��:ʹ����delegatge���� Ҳ����: Ѱ��һ����Դʱ,����Ѱ��ϵͳ����ClassLoader�������Դ,������webapp�Լ�����Դ
 * 
 * ����:�����ͨ��webres����webapp�Լ�����Դʱ,����ʹ���Լ�����Դ����,�Ա����ϵͳ��Դ����
 * 
 * @author Jason Zhu
 */
public class WebRes extends HttpServlet
{
	/**
	 * ���������ñ��������ڵ�Webapp�����ĸ�·��,���� '/' ����<br/>
	 * <br/>
	 * �÷�������֧��ҳ���е���������ҳ�����ݵľ���·������<br/>
	 * ����,һ��jsp�ļ���������ĳһͼƬ,����jsp�ᱻ����jsp����,Ϊ��ʹ������ʱ��ص�ͼƬ�����ܹ������ʵ�<br/>
	 * ��jsp��ʵ��ʱ,����ͨ����̬�ķ����Դ�·�����м���
	 * 
	 * @param req
	 * @return
	 */
	public static String getContextRootPath(HttpServletRequest req)
	{
		String s = req.getContextPath() ;
		if("/".equals(s))
			return s ;
		
		return s + "/";
	}
	/**
	 * ����֧���ļ�����,��ͼƬ���չʾ�ķ���
	 * 
	 * @param resp
	 * @param filename
	 * @param file_cont
	 * @throws IOException
	 */
	public static void renderFile(HttpServletResponse resp, String filename,
			byte[] file_cont) throws IOException
	{
		renderFile(resp, filename,file_cont,false);
	}
	
	public static void renderFile(HttpServletResponse resp, String filename,
			byte[] file_cont,boolean showpic) throws IOException
	{
		renderFile(null,resp, filename,
				file_cont,showpic,null);
	}
	
	public static void renderFile(HttpServletRequest req,HttpServletResponse resp, String filename,
			byte[] file_cont,boolean showpic,Date lastmodify) throws IOException
	{
		if (file_cont == null)
			return;
		
//		if(!showpic)
//			resp.addHeader("Content-Disposition", "attachment; filename=\""+MimeUtility.encodeText(filename,"UTF-8",null)+"\"");
		filename = filename.replace(" ", "") ;
		if(!showpic)
			resp.addHeader("Content-Disposition", "attachment; filename="+ new String(filename.getBytes(),"iso8859-1"));
		
		
		if(lastmodify!=null)
		{
			resp.setDateHeader("Last-Modified", lastmodify.getTime()) ;
		}
		
		if(req!=null)
		{
			long l = req.getDateHeader("If-Modified-Since") ;
			if(l>0)
			{
				if(lastmodify!=null&&lastmodify.getTime()/1000<=l/1000)
				{//�����ļ�ϵͳ������޸�ʱ�侫ȷ����,������Ҫȥ�������Ա��ڼ���
					resp.setStatus(304) ;
					return ;
				}
			}
		}
		
		
		resp.setContentType(Mime.getContentType(filename));
		ServletOutputStream os = resp.getOutputStream();
		os.write(file_cont);
		os.flush();
	}
	
	/**
	 * ����֧���ļ�����,��ͼƬ���չʾ�ķ���
	 * 
	 * @param resp
	 * @param filename
	 * @param cont_stream
	 * @throws IOException
	 */
	public static void renderFile(HttpServletResponse resp, String filename,
			InputStream cont_stream) throws IOException
	{
		renderFile(resp, filename,cont_stream,false);
	}
	
	public static void renderFile(HttpServletResponse resp, String filename,
			InputStream cont_stream,boolean showpic) throws IOException
	{
		renderFile(resp, filename,
				cont_stream,showpic,true);
	}
	
	public static void renderFile(HttpServletResponse resp, String filename,
			InputStream cont_stream,boolean showpic,boolean ignorespace) throws IOException
	{
		if (cont_stream == null)
			return;
		
		if(ignorespace)
			filename = filename.replace(" ", "") ;
		
		if(!showpic)
			resp.addHeader("Content-Disposition", "attachment; filename="+ new String(filename.getBytes(),"iso8859-1"));
		
		resp.setContentType(Mime.getContentType(filename));
		ServletOutputStream os = resp.getOutputStream();
		byte[] buf = new byte[1024];
		int len = 0 ;
		while((len=cont_stream.read(buf))!=-1)
		{
			os.write(buf,0,len);
		}
		
		os.flush();
	}
	
	public static void renderXormFile(
			HttpServletRequest req,HttpServletResponse resp,
			Class xormc,String xormprop,long pkid,
			String filename)
	throws ClassNotFoundException, Exception
	{
		renderXormFile(
				req,resp,
				xormc,xormprop,pkid,
				filename,false,null);
	}
	
	public static void renderXormFile(
			HttpServletRequest req,HttpServletResponse resp,
			Class xormc,String xormprop,long pkid,
			String filename,boolean showpic,Date lastmodify)
	throws ClassNotFoundException, Exception
	{
		filename = filename.replace(" ", "") ;
		
		if(!showpic)
			resp.addHeader("Content-Disposition", "attachment; filename="+ new String(filename.getBytes(),"iso8859-1"));
		
		if(lastmodify!=null)
		{
			resp.setDateHeader("Last-Modified", lastmodify.getTime()) ;
		}
		
		if(req!=null)
		{
			long l = req.getDateHeader("If-Modified-Since") ;
			if(l>0)
			{
				if(lastmodify!=null&&lastmodify.getTime()/1000<=l/1000)
				{//�����ļ�ϵͳ������޸�ʱ�侫ȷ����,������Ҫȥ�������Ա��ڼ���
					resp.setStatus(304) ;//not modify
					return ;
				}
			}
		}
		
		GDB g = GDB.getInstance() ;
		
		long filelen = g.getXORMFileContLength(xormc, xormprop,
				pkid);
		if(filelen<0)
			return ;
		
		resp.setContentLength((int)filelen);
		resp.setContentType(Mime.getContentType(filename));
		ServletOutputStream os = resp.getOutputStream();
		GDB.getInstance().loadXORMFileContToOutputStream(xormc, xormprop,
				pkid,os) ;
		os.flush();
	}
	
	/**
	 * ����XORM����Ͷ�Ӧ���ļ�������,����ļ�����.
	 * 
	 * @param resp
	 * @param filename
	 * @param xorm_c
	 * @param xorm_prop
	 * @param xorm_pk
	 */
	public static void renderFile(HttpServletResponse resp, String filename,Class xorm_c,String xorm_prop,long xorm_pk)
		throws Exception
	{
		InputStream is = null;
		try
		{
			is = GDB.getInstance().getXORMFileStreamByPkId(xorm_c,xorm_prop, xorm_pk);
			if(is==null)
				return ;
			
			renderFile(resp, filename,is) ;
		}
		finally
		{
			if(is!=null)
				is.close();
		}
	}
	
	/**
	 * ��������Ķ���,������Ĳ�����ϳ��µ�url
	 * �÷�������Ϊ��ҳ�ṩ�Զ��������֧��
	 * @param req
	 * @param pname
	 * @param pval
	 * @return
	 */
	public static String combineRequestUrlWithParam(HttpServletRequest req,String pname,String pval)
	{
		return "";
	}

	private static HashMap<String, byte[]> res2cont = new HashMap<String, byte[]>();

	private static HashMap<String,String> res2txt_cont = new HashMap<String,String>() ;
	
	// public static String getAbsResUrl(String related_res)
	// {
	//		
	// }
	
	/**
	 * ������Դ·��,��ö�Ӧ���ļ��ı�����
	 */
	public static String getResTxtContent(String p)
		throws IOException
	{
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			return getResTxtContent(cl,p);
	}
	
	public static String getResTxtContent(ClassLoader cl,String p)
		throws IOException
	{
		String s = res2txt_cont.get(p);
		if(s!=null)
			return s ;
		
		InputStream is = null;
		try
		{
			is = cl.getResourceAsStream(p);
			// is = this.getClass().getResourceAsStream(p);
			if (is == null)
				return null;

			byte[] buf = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			while ((len = is.read(buf)) >= 0)
			{
				baos.write(buf, 0, len);
			}

			byte[] cont = baos.toByteArray();
			s = new String(cont,"UTF-8");
			
			res2txt_cont.put(p, s);
			return s ;
		}
		finally
		{
			if (is != null)
				is.close();
		}
	}

	private ClassLoader appCL = null;

	public WebRes()
	{
	}

	public void init() throws ServletException
	{
		// ��õ�ǰwebapp��class loader
		appCL = Thread.currentThread().getContextClassLoader();
	}
	
	private InputStream getInputStreamByPath(String p)
	{
		InputStream is = appCL.getResourceAsStream(p);
		if(is!=null)
			return is ;
		
		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(p);
		if(is!=null)
			return is ;
		
		ClassLoader mycl = this.getClass().getClassLoader() ;
		URL u = mycl.getResource(p);
		is = mycl.getResourceAsStream(p);
		if(is!=null)
			return is ;
		
		ClassLoader pcl = mycl ;
		while((pcl=pcl.getParent())!=null)
		{
			is = pcl.getResourceAsStream(p);
			if(is!=null)
				return is ;
		}
		return null ;
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException
	{
		String p = req.getParameter("r");
		if (p == null || (p = p.trim()).equals(""))
		{
			String pi = req.getPathInfo() ;
			
			return;
		}

		byte[] cont = res2cont.get(p);
		if (cont != null)
		{
			resp.setContentType(Mime.getContentType(p));
			ServletOutputStream os = resp.getOutputStream();
			os.write(cont);
			os.flush();
			return;
		}

		InputStream is = null;
		try
		{
			is = appCL.getResourceAsStream(p);//getInputStreamByPath(p) ;
			if(is==null)
			{
				is = new Object().getClass().getResourceAsStream(p) ;
				if(is==null)
					return ;
			}

			byte[] buf = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			while ((len = is.read(buf)) >= 0)
			{
				baos.write(buf, 0, len);
			}

			cont = baos.toByteArray();
			res2cont.put(p, cont);
			resp.setContentType(Mime.getContentType(p));
			ServletOutputStream os = resp.getOutputStream();
			os.write(cont);
			os.flush();
			return;
		}
		finally
		{
			if (is != null)
				is.close();
		}
	}
}
