package com.dw.system ;

import javax.servlet.http.* ;
import javax.servlet.* ;
import java.net.* ;

/**
 * 本Servlet负责在应用服务器启动时，读取configuration文件。<br>
 * 应当在web.xml中配置Servlet如下：<pre><b>
 * &lt;servlet>
 *    &lt;servlet-name>ConfigureLoaderServlet&lt;/servlet-name>
 *    &lt;servlet-class>com.dw.system.ConfigureLoaderServlet&lt;/servlet-class>
 *    &lt;init-param>
 *        &lt;param-name>config&lt;/param-name>
 *        &lt;param-value>/WEB-INF/application.properties&lt;/param-value>
 *    &lt;/init-param>
 *    &lt;load-on-startup>1&lt;/load-on-startup>
 * &lt;/servlet>
 * </b></pre>
 */
public class ConfigureLoaderServlet extends HttpServlet
{
	public void init (ServletConfig config)
		throws ServletException
	{
		// Configuration.load ("") ;
		String file = config.getInitParameter ("config") ;
		if (file == null)
		{
			System.out.println ("No init parameter [config] specified.") ;
			return ;
		}
		System.out.print ("Will loading Configuration from [" + file + "] ... ") ;
		try
		{
			URL url = config.getServletContext ().getResource (file) ;
			Configuration.load (url) ;
			System.out.println ("done!") ;
		}
		catch (Throwable t)
		{
			System.out.println ("Error occurs: ") ;
			t.printStackTrace () ;
		}


	}
}
