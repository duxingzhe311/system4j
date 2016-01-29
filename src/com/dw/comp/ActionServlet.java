package com.dw.comp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dw.system.util.Mime;

/**
 * 支持Action的Servlet
 * @author Jason Zhu
 */
public class ActionServlet extends HttpServlet
{
	private static HashMap<String, byte[]> res2cont = new HashMap<String, byte[]>();

	// public static String getAbsResUrl(String related_res)
	// {
	//		
	// }
	
	private ClassLoader appCL = null ;

	public ActionServlet()
	{
	}

	public void init() throws ServletException
	{
		//获得当前webapp的class loader
		appCL = Thread.currentThread().getContextClassLoader();
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException
	{
		String p = req.getParameter("r");
		if (p == null || (p = p.trim()).equals(""))
			return;

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
			is = appCL.getResourceAsStream(p);
			//is = this.getClass().getResourceAsStream(p);
			if (is == null)
				return;

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
