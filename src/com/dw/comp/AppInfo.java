package com.dw.comp;

import java.io.File;
import java.io.FilenameFilter;

public class AppInfo
{
	String realPath = null ;
	String contextName = null ;
	ClassLoader relatedCl = null ;
	
	public AppInfo(String realpath,String context_name,ClassLoader cl)
	{
		this.realPath = realpath ;
		this.contextName = context_name ;
		relatedCl = cl ;
	}
	
	public String getRealPath()
	{
		return realPath ;
	}
	
	public String getContextName()
	{
		return contextName;
	}
	
	public ClassLoader getRelatedClassLoader()
	{
		return relatedCl ;
	}
	
	static FilenameFilter fnf = new FilenameFilter()
	{

		public boolean accept(File dir, String name)
		{
			return name.toLowerCase().endsWith(".jar");
		}};
	transient private String relatedCP = null ;
	/**
	 * 得到WEB-INF下的相关类路径
	 * @return
	 */
	public String getRelatedClassPath()
	{
		if(relatedCP!=null)
			return relatedCP ;
		
		StringBuilder sb = new StringBuilder();
		
		String ps = System.getProperty("path.separator");
		String p1 = realPath+"WEB-INF/classes" ;
		
		if(new File(p1).exists())
		{
			sb.append(ps).append(p1);
		}
		String p2 = realPath +"WEB-INF/lib/" ;
		File libf = new File(p2);
		if(libf.exists())
		{
			for(File f:libf.listFiles(fnf))
			{
				sb.append(ps).append(f.getAbsolutePath());
			}
		}
		
		relatedCP = sb.toString() ;
		return relatedCP;
	}
	
	public String toString()
	{
		return contextName+"+"+realPath;
	}
}
