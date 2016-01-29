package com.dw.system.util ;

import java.io.* ;
import java.net.* ;
import java.applet.* ;
import java.awt.* ;


public class URLMaker
{
	static boolean inJarFile = false ;
	
	public static URL make (String fileName)
	{
		return make (fileName , null) ;
	}
	/**
	 * Convert a file name to a particular URL,
	 * if you call this from a Applet, the Component 
	 * be used to locate applet's codebase, and make a URL;
	 * else a local file URL be created.<br/>
	 * If InjarFile flag be set, will load file from class path(jar file).
	 */
	public static URL make (String fileName , Component comp)
	{
		if (inJarFile)
		{
			if (! fileName.startsWith ("/"))
				fileName = "/" + fileName ;
			return new Object () {}.getClass ().getResource (fileName) ;
		}
		
		while (comp != null)
		{
			if (comp instanceof Applet)
			{
				URL codeBase = ((Applet) comp).getCodeBase () ;
				if (codeBase != null)
				{
					try
					{
						return new URL (codeBase , fileName) ;
					}
					catch (MalformedURLException _mue)
					{
						throw new RuntimeException ("URL Format error. " + 
							codeBase + "[" + fileName + "]") ;
					}
				}
			}
			
			comp = comp.getParent () ;
		}
		
		try
		{
			return new File (fileName).toURL () ;
		}
		catch (MalformedURLException _mue)
		{
			throw new RuntimeException ("URL Format error. [" + 
				fileName + "]") ;
		}
	}
	
	public static InputStream open (String fileName , Component comp)
		throws IOException 
	{
		return make (fileName , comp).openStream () ;
	}
	
	public static void inJarFile (boolean bol)
	{
		inJarFile = bol ;
	}
}