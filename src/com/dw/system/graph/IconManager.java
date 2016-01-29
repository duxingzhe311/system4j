package com.dw.system.graph;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * <p>
 * Title: 工作流引擎
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * Created on 2004-6-20
 * 
 * @author Jason Zhu
 * @version 2.0
 */
public class IconManager
{
//	static Hashtable	images;

	static
	{
//		images = new Hashtable();
//		URL u = IconManager.class.getResource("res.prop");
//		InputStream is = null ;
//		
//		try
//		{
//			is = u.openStream() ;
//			Properties prop = new Properties();
//			prop.load(is);
//			
//			for(Enumeration en = prop.propertyNames() ; en.hasMoreElements() ;)
//			{
//				String n = (String)en.nextElement() ;
//				String p = prop.getProperty(n);
//				
//				try
//				{
//					//System.out.println("Load image="+p);
//					ImageIcon ii = new ImageIcon(IconManager.class
//						.getResource(p));
//					
//					images.put(n, ii);
//				}
//				catch(Exception ee)
//				{
//					System.out.println("<Warning>:get image error:"+p);
//					//ee.printStackTrace();
//				}
//			}
//		}
//		catch(Exception e)
//		{
//			
//		}
//		finally
//		{
//			if(is!=null)
//			{
//				try
//				{
//					is.close();
//				}
//				catch(IOException ioe)
//				{
//					
//				}
//			}
//		}
	}
	
//	public static ImageIcon getGIcon(String s)
//	{
//		return (ImageIcon) images.get(s);
//	}

	public IconManager()
	{
	}

	
	
	Class belongC = null ;

	Hashtable localImgs = new Hashtable();
	/**
	 * 在
	 * @param c
	 */
	public IconManager(Class c,String propfile)
	{
		belongC = c ;
		
		URL u = belongC.getResource(propfile);
		InputStream is = null ;
		
		try
		{
			is = u.openStream() ;
			Properties prop = new Properties();
			prop.load(is);
			
			for(Enumeration en = prop.propertyNames() ; en.hasMoreElements() ;)
			{
				String n = (String)en.nextElement();
				String p = prop.getProperty(n).trim();
				n = n.trim();
				
				try
				{
					
					URL uu = belongC.getResource(p) ;
					//System.out.println("Load image="+p+"  "+uu);
					if(uu!=null)
					{
						ImageIcon ii = new ImageIcon(uu);
						localImgs.put(n, ii);
					}
				}
				catch(Exception ee)
				{
					System.out.println("<Warning>:get image error:"+p);
					ee.printStackTrace();
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
	
	public ImageIcon getIcon(String name)
	{
		return (ImageIcon) localImgs.get(name);
	}
}
