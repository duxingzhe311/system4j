package com.dw.system ;

import java.io.File ;
import java.io.FileInputStream;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.FileNotFoundException ;
import java.io.StringReader ;
import java.util.Vector ;
import java.util.Properties ;
import java.util.Enumeration ;
import java.net.* ;
/**
 * 本类从配置文件中读取配置信息，以系统参数的形式保存。<br>
 * 注意：由于java.util.Properties类的缺陷，配置文件中不能有中文。<br>
 * 本类在初始化时会在CLASSPATH中搜索<b>application.properties</b> 文件。
 * 并将该文件中定义的配置信息加入到System Properties中，可以使用
 * <b>System.getPeroperty()</b>访问这些属性。
 */

public class Configuration
{
	private static char [] BLOCK_CHAR = {'{' , '}'} ;

	/**
	 * 缺省配置文件名(app.conf)。
	 */
	public static final String DEFAULT_CONFIG_FILE = "app.conf" ;

	private static Vector guests = new Vector () ;

	private static String fileName = null ;

	private static Vector parsingNames = new Vector () ;
	private static Vector parsedNames = new Vector () ;

	private static Properties p = new Properties () ;

	private static boolean loaded = false ;

	/**
	 * 读缺省配置文件信息。
	 * @return	返回配置文件属性内容
	 * @see	#DEFAULT_CONFIG_FILE
	 */
	public static synchronized Properties load ()
	{
		if (loaded)
			return p ;
		
		String sysconffn = System.getProperties().getProperty("user.dir")+"/tomato.conf";
		File f = new File(sysconffn);
		if(f.exists())
		{
			Properties tmpp = new Properties();
			FileInputStream fis = null ;
			
			try
			{
				fis = new FileInputStream(f);
				tmpp.load(fis);
				
				Properties sysp = System.getProperties();
				for(Enumeration en = tmpp.propertyNames();en.hasMoreElements();)
				{
					String k = (String)en.nextElement();
					String v = tmpp.getProperty(k);
					sysp.setProperty(k, v);
				}
				System.setProperties(sysp);
			}
			catch(Exception ioe)
			{
				ioe.printStackTrace();
			}
			finally
			{
				if(fis!=null)
				{
					try
					{
						fis.close();
					}
					catch(Exception eee)
					{}
				}
			}
		}
		
		//System.out.print ("Will Search [/" + DEFAULT_CONFIG_FILE + "] in class path ... ") ;
		URL url = new Object () {}.getClass ().getResource ("/" + DEFAULT_CONFIG_FILE) ;

		if (url == null)
		{
			//System.out.println ("File not found! ") ;
			return p ;
		}
		//System.out.println ("find [" + url.toString () + "].") ;
		//System.out.print ("parsing ... ") ;
		try
		{
			load (url) ;
		}
		catch (Throwable t)
		{
			System.out.println ("Error occurs: ") ;
			t.printStackTrace () ;
			return p ;
		}

		//System.out.println ("done.") ;
		return p ;
	}
	/**
	 * 使用指定文件刷新系统参数表。
	 * @see	#refresh(String)
	 */
	public synchronized static Properties load (URL url) throws IOException
	{

		if (url == null)
			throw new FileNotFoundException ("Configuration file not found.") ;
		InputStream in = url.openStream () ;
		if (in == null)
			throw new FileNotFoundException ("Configuration file not found, [" + url + "]") ;

		parsingNames.clear () ;
		parsedNames.clear () ;
		p.clear () ;

		p.load (in) ;

		try
		{
			in.close () ;
		}
		catch (Throwable t)
		{
			System.err.println ("Error occurs during close file [" + url + "]") ;
			t.printStackTrace () ;
		}
		p.setProperty ("current.location" , new File (url.getFile ()).getParentFile ().getAbsolutePath ()) ;
		// System.out.println () ;

		Enumeration e = p.propertyNames () ;

		Properties sys = System.getProperties () ;

		while (e.hasMoreElements ())
		{
			String name = (String) e.nextElement () ;
			String value = p.getProperty (name) ;

			value = parseValue (name , value) ;

			if (value != null)
			{
				// System.out.println (name + "=" + value) ;
				// p.setProperty (name , value) ;
				sys.setProperty (name , value) ;
			}

		}

		// p.list (System.out) ;
		System.setProperties (sys) ;

		if (! guests.isEmpty ())
		{
			EnableAutoConfig eacs [] = new EnableAutoConfig [guests.size ()] ;
			int i = 0 ;
			for (i = 0 ; i < guests.size () ; i ++)
			{
				eacs [i] = (EnableAutoConfig) guests.elementAt (i) ;
			}

			guests.removeAllElements () ;

			// Auto update
			for (i = 0 ; i < eacs.length ; i ++)
			{
				if (eacs [i] != null)
				{
					eacs [i].autoConfig () ;
					// System.out.println ("EAC is " + eacs [i].toString () + " [" + i + " > " + guests.size () + "]") ;
				}
			}
		}

		loaded = true ;
		return p ;
	}


	private static String parseValue (String name , String value) throws IOException
	{
		if (parsedNames.contains (name))
			return value ;
		if (parsingNames.contains (name))
			throw new NestedReferenceException (
				"Nested reference in configration file \"" +
				fileName + "\"" + parsingNames.toString () + "--> " + name) ;

		parsingNames.addElement (name) ;

		String value0 = "" ;

		StringReader in = new StringReader (value) ;
		int c = in.read () ;
		while (c != -1)
		{
			// System.out.print ((char) c) ;
			if (c == BLOCK_CHAR [0] || c == BLOCK_CHAR [1])
			{
				int ch = in.read () ;

				if (ch == c)
					value0 += (char) c ;
				else
				if (c == BLOCK_CHAR [0])
				{
					c = ch ;
					String name0 = "" ;
					while (c != -1)
					{
						if (c == BLOCK_CHAR [0])
						{
							c = in.read () ;
							if (c != BLOCK_CHAR [0])
							{
								name0 += (char) c ;
							}
							// else ignore
						}
						else
						if (c == BLOCK_CHAR [1])
						{
							c = in.read () ;
							if (c == BLOCK_CHAR [1])
							{
								name0 += (char) c ;
							}
							else
								break ;
						}
						else
							name0 += (char) c ;

						c = in.read () ;
					}

					if (! name0.equals (""))
					{
						// System.out.println (name0) ;
						if (p.containsKey (name0))
						{
							value0 += parseValue (name0 , (String) p.getProperty (name0)) ;
						}
					}
					if (c != -1)
						continue ;
				}

			}
			else
				value0 += (char) c ;

			c = in.read () ;
		}

		p.setProperty (name , value0) ;

		parsingNames.removeElement (name) ;

		parsedNames.addElement (name) ;

		return value0 ;

	}
	/**
	 * 实现自动配置的对象可以调用该方法注册。
	 */
	public static void register (EnableAutoConfig e)
	{
		if (! guests.contains (e))
			guests.addElement (e) ;
	}
	/**
	 * 析构函数。
	 */
	protected void finalize ()
	{
		guests.clear () ;

		guests = null ;
	}

	/**
	 * 根据名称获取配置信息。
	 * @param key 配置信息名称，如：application.home。
	 * @return 配置信息的值。在配置文件中以name =|: value表示。
	 */
	public static String getProperty (String key)
	{
		load () ;

		if (p != null)
			return (String) p.get (key) ;
		return null ;
	}
	
	public static String[] getPropertyNamesByPrefix(String prefix)
	{
		load ();
		
		if(p==null)
			return null;
		
		Vector v = new Vector();
		
		for(Enumeration en = p.keys();en.hasMoreElements();)
		{
			String key = (String)en.nextElement();
			if(prefix==null||key.startsWith(key))
				v.addElement(key);
		}
		
		String[] rets = new String[v.size()];
		v.toArray(rets);
		return rets ;
	}
	
	public static Properties getSubPropertyByPrefix(String prefix)
	{
		String[] pns = getPropertyNamesByPrefix(prefix);
		if(pns==null)
			return null ;
		
		Properties tmpp = new Properties();
		for(int i = 0 ; i < pns.length ; i ++)
		{
			tmpp.setProperty(pns[i].substring(prefix.length()), getProperty(pns[i]));
		}
		return tmpp ;
	}

}
/**
 * 运行期异常，在发现文件中包含不可析的嵌套定义时被抛出。
 */
class NestedReferenceException extends RuntimeException
{
	public NestedReferenceException (String message)
	{
		super (message) ;
	}
}