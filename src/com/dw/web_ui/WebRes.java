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
 * 通过资源路径的方式获得资源内容。该servlet可以支持获得 资源文件内容，如公共图片等.这些图片被打包到jar文件中或和class在同一个目录中
 * 
 * 该类重载了init方法,并在里面获得了当前webapp的ClassLoader,这样,WebRes在寻找资源时和class loader
 * 保持一致:使用了delegatge机制 也就是: 寻找一个资源时,首先寻找系统级的ClassLoader里面的资源,最后才是webapp自己的资源
 * 
 * 所以:如果想通过webres访问webapp自己的资源时,尽量使用自己的资源名称,以避免和系统资源重名
 * 
 * @author Jason Zhu
 */
public class WebRes extends HttpServlet
{
	/**
	 * 根据请求获得本请求所在的Webapp上下文根路径,它以 '/' 结束<br/>
	 * <br/>
	 * 该方法用来支持页面中的引用其他页面内容的绝对路径生成<br/>
	 * 比如,一个jsp文件中引用了某一图片,但该jsp会被其他jsp调用,为了使被调用时相关的图片引用能够被访问到<br/>
	 * 该jsp在实现时,必须通过动态的方法对此路径进行计算
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
	 * 用来支持文件下载,或图片输出展示的方法
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
				{//由于文件系统的最后修改时间精确到秒,所以需要去除毫秒以便于计算
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
	 * 用来支持文件下载,或图片输出展示的方法
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
				{//由于文件系统的最后修改时间精确到秒,所以需要去除毫秒以便于计算
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
	 * 根据XORM定义和对应的文件属性列,输出文件内容.
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
	 * 根据请求的对象,和输入的参数组合出新的url
	 * 该方法可以为翻页提供自动参数组合支持
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
	 * 根据资源路径,获得对应的文件文本内容
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
		// 获得当前webapp的class loader
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
