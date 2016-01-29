package com.dw.system.xmldata.xrmi;

import com.dw.system.xmldata.IXmlDataable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.lang.annotation.*;

import javax.swing.ImageIcon;

public class XRmiClassRegister
{
	static Hashtable<String,Class> regname2c = null;
	static Hashtable<Class,String> c2regname = null;
	
	static
	{
		regname2c = new Hashtable<String,Class>() ;
		c2regname = new Hashtable<Class,String>() ;
		
		findRegConfigFileInClassPath();
	}
	
	private static void findRegConfigFileInClassPath()
	{
		URL u = XRmiClassRegister.class.getResource("/xrmi_class.conf");
		if(u==null)
			return ;
		
		InputStream is = null ;
		
		try
		{
			is = u.openStream() ;
			Properties prop = new Properties();
			prop.load(is);
			
			for(Enumeration en = prop.propertyNames() ; en.hasMoreElements() ;)
			{
				String n = (String)en.nextElement() ;
				n = n.trim();
				if(n.startsWith("#"))
					continue ;
				
				try
				{
					Class c = Class.forName(n);
					if(c==null)
						continue;
					
					XRmi xrmi = (XRmi)c.getAnnotation(XRmi.class);
					if(xrmi==null)
						continue ;
					
					String regname = xrmi.reg_name() ;
					if(regname==null||regname.equals(""))
						continue ;
					
					System.out.println("XRmi reg:"+regname+":"+c.getCanonicalName());
					registerIXmlDataableClass(regname,c) ;
				}
				catch(Exception eee)
				{
					//eee.printStackTrace();
					System.out.println("register XRmi class failed:"+n);
				}
			}
		}
		catch(Exception e)
		{
			
		}
		finally
		{
			if(is!=null)
			{
				try
				{
					is.close();
				}
				catch(IOException ioe)
				{
					
				}
			}
		}
	}
	
	
	
	public static void registerIXmlDataableClass(String regname,String classname)
		throws Exception
	{
		Class c = Class.forName(classname);
		
		if(!isIXmlDataableClass(c))
			throw new Exception("class:"+classname+" is not implements IXmlDataable");
		
		regname2c.put(regname, c);
		c2regname.put(c, regname);
	}
	
	public static void registerIXmlDataableClass(String regname,Class c)
	{
		if(!isIXmlDataableClass(c))
			throw new IllegalArgumentException("class:"+c.getName()+" is not implements IXmlDataable");
		
		regname2c.put(regname, c);
		c2regname.put(c, regname);
	}
	
	private static boolean isIXmlDataableClass(Class c)
	{
		return IXmlDataable.class.isAssignableFrom(c) ;
//		if(c.isInterface()&&c==IXmlDataable.class)
//		{
//			return true ;
//			
//		}
//		
//		for(Class tmpc : c.getInterfaces())
//		{
//			if(tmpc==IXmlDataable.class)
//			{
//				return true ;
//			}
//		}
//		
//		return false;
	}
	
	public static String getRegNameByClass(Class c)
	{
		return c2regname.get(c);
	}
	
	public static IXmlDataable createNewInstanceByRegName(String regname)
		throws Exception
	{
		Class c = regname2c.get(regname);
		if(c==null)
			return null ;
		
		return (IXmlDataable)c.newInstance();
	}
}
