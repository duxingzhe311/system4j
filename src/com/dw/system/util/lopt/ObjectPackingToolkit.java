package com.dw.system.util.lopt ;
// ------------------------------------
// Create at 2001.08.28
// Tab char size : 4
// ------------------------------------
import java.lang.reflect.* ;
import java.util.* ;

import com.dw.system.util.* ;
import java.io.* ;
/**
 * To packing a object, using LOPT. the primitive data type will be
 * proceeded by {@link com.css.util.Message}. <br/>
 * <b>Primitive Data Type include: </b><br/>
 * 1. byte.<br/>
 * 2. char.<br/>
 * 3. int.<br/>
 * 4. long.<br/>
 * 5. float.<br/>
 * 6. double.<br/>
 * 7. String.<br/>
 * 8. Primitive and Object array.<br/>
 * Class Packing Description based on LOPT be write in a file, and be
 * loaded within a static action. <br/>
 * <b>The LOPT Description Format: </b><br/>
 * <i>ObjectClassFullName</i> = <i>LOPT Expression</i> ; <i>LOPT Operations</i><sub><i>Opt</i></sub>.<br/>
 * <i>LOPT Operations</i> = {
 			<i>LOPT Expression</i> ;
 *		}
 * @version 1.0.1
 */
public class ObjectPackingToolkit
{
	static Hashtable classPackingDefs ;

	/**
	 * Pack a Object into a {@link com.css.util.Message}. <br/>
	 * <b.Format: </b><br/>
	 * 1. Class name of Object.<br/>
	 * following the internal, non-resolved value.
	 */
	public static void packObject (Object obj , Message msg)
		throws JERuntimeException
	{
		Class c = obj.getClass () ;

		PackingDescription pd = (PackingDescription) classPackingDefs.get (c) ;

		if (pd == null)
		{
			if (obj instanceof Serializable)
			{
				packSerializableObject ((Serializable) obj , msg) ;
				return ;
			}

			// use simple bean pack method
			packSimpleBean (obj , msg) ;
			return ;
			/*
			throw new IllegalArgumentException ("No description for class [" +
				c.getName () + "]") ;
			*/
		}
		Vector v = pd.pack (obj) ;
		msg.clear () ;
		msg.add (c.getName ()) ;
		msg.add (PACK_METHOD_LOPT) ;
		for (int i = 0 ; i < v.size () ; i ++)
			msg.add (v.elementAt (i)) ;

	}

	/**
	 * Extract a object from a {@link com.css.util.Message}.
	 */
	public static Object extractObject (Message msg)
		throws JERuntimeException
	{
		int packMethod = ((Integer) msg.get (1)).intValue () ;

		switch (packMethod)
		{
		case PACK_METHOD_LOPT :
			return extractLoptObject (msg) ;
		case PACK_METHOD_SIMPLE_BEAN :
			return extractSimpleBean (msg) ;
		case PACK_METHOD_SERIALIZABLE :
			return extractSerializableObject (msg) ;
		default :
			throw new JERuntimeException ("Unknown Pack Method[" +
				packMethod + "], Message format Error!") ;
		}
	}
	/**
	 * Extract an Object from Message using LOPT method.
	 * @since 1.0.1
	 */
	public static Object extractLoptObject (Message msg)
	{
		Class c = null ;
		String className = (String) msg.get (0) ;

		try
		{
			c = Class.forName (className) ; // , false , null) ;
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;

			throw new JERuntimeException ("Can't load class [" + className + "]") ;
		}

		PackingDescription pd = (PackingDescription) classPackingDefs.get (c) ;

		if (pd == null)
		{
			throw new IllegalArgumentException ("No description for class [" +
				c.getName () + "]") ;
		}
		Vector v = new Vector (msg.getSize ()) ;

		for (int i = 2 ; i < msg.getSize () ; i ++)
			v.addElement (msg.get (i)) ;

		return pd.extract (v) ;
	}

	/**
	 * Create a new instance of specified class.
	 */
	public static Object newInstance (String className)
		throws JERuntimeException
	{
		Class c = null ;

		try
		{
			c = Class.forName (className) ; // , false , null) ;
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;

			throw new JERuntimeException ("Can't load class [" + className + "]") ;
		}

		PackingDescription pd = (PackingDescription) classPackingDefs.get (c) ;

		if (pd == null)
			throw new IllegalArgumentException ("No description for class [" +
				c.getName () + "]") ;
		return pd.newInstance () ;
	}

	private static final int PACK_METHOD_SIMPLE_BEAN = 1 ;
	private static final int PACK_METHOD_SERIALIZABLE = 2 ;
	private static final int PACK_METHOD_LOPT = 0 ;

	/**
	 * Pack a serializable Object into Message Object. <br/>
	 * @since 1.0.1
	 */
	public static void packSerializableObject (Serializable obj , Message msg)
	{
		if (!(obj instanceof Serializable))
			throw new JERuntimeException (obj.getClass ().getName () +
				" Not a Serializable Object!") ;
		try
		{
			msg.add (obj.getClass ().getName ()) ;
			msg.add (PACK_METHOD_SERIALIZABLE) ;
			ByteArrayOutputStream bout = new ByteArrayOutputStream (4*1024) ;

			ObjectOutputStream out = new ObjectOutputStream (bout) ;

			out.writeObject (obj) ;
			out.flush () ;
			out.close () ;
			msg.add (bout.toByteArray ()) ;

		}
		catch (IOException _ioe)
		{
			_ioe.printStackTrace () ;

			throw new JERuntimeException ("Unknow Error during pack Serializable Object: " +
				_ioe.getClass ().getName () + "->" +
				_ioe.getMessage ()) ;
		}
	}

	/**
	 * Extract a serializable Object from Message. <br/>
	 * @since 1.0.1
	 */
	public static Object extractSerializableObject (Message msg)
	{
		Object obj = msg.get (2) ;
		if (!(obj instanceof byte []))
			throw new RuntimeException ("Data Format Error, Packed Data MUST be a Byte Array!") ;
		byte [] bytes = (byte []) obj ;

		try
		{
			ObjectInputStream in = new ObjectInputStream (new ByteArrayInputStream (bytes)) ;
			return in.readObject () ;
		}
		catch (ClassNotFoundException _cnfe)
		{
			throw new JERuntimeException ("Can't found class during extrcat Serializable Object: " +
				_cnfe.getMessage ()) ;
		}
		catch (IOException _ioe)
		{
			throw new JERuntimeException ("Unknown Error: " +
				_ioe.getClass ().getName () + "->" +
				_ioe.getMessage ()) ;
		}


	}
	/**
	 * Pack an Object into Message using Simple Bean method. <br/>
	 * @since 1.0.1
	 */
	public static void packSimpleBean (Object bean , Message msg)
	{
		Class beanClass = bean.getClass () ;
		msg.add (beanClass.getName ()) ;
		msg.add (PACK_METHOD_SIMPLE_BEAN) ;
		java.lang.reflect.Field [] fields = beanClass.getDeclaredFields () ;
		try
		{
			AccessibleObject.setAccessible (fields , true) ;
		}
		catch (SecurityException _se)
		{
			fields = beanClass.getFields () ;
		}

		for (int k = 0 ; k < fields.length ; k ++)
		{
			java.lang.reflect.Field field = fields [k] ;

			if (Modifier.isFinal (field.getModifiers ()))
				continue ;
			try
			{
				Object value = field.get (bean) ;
				String name = field.getName () ;

				//System.out.println ("Field [" + name + "=" + value + "]") ;
				msg.add (name , value) ;
			}
			catch (IllegalAccessException _iae)
			{
				_iae.printStackTrace () ;
			}
		}

		return ;
	}

	/**
	 * Extract an Object from Message using Simple Bean method. <br/>
	 * @since 1.0.1
	 */
	public static Object extractSimpleBean (Message msg)
	{
		String className = (String) msg.get (0) ;
		Class beanClass = null ;
		try
		{
			beanClass = Class.forName (className) ;
		}
		catch (ClassNotFoundException _cnfe)
		{
			throw new JERuntimeException ("Can't found class [" + className +
				"] during extrcat simple bean!") ;
		}

		Object bean = null ;
		try
		{
			bean = beanClass.newInstance () ;
		}
		catch (InstantiationException _ie)
		{
			_ie.printStackTrace () ;
			throw new JERuntimeException ("Can't make instance of class [" +
				className + "], maybe this class have no default constructor!") ;
		}
		catch (IllegalAccessException _iae)
		{
			_iae.printStackTrace () ;
			throw new JERuntimeException ("Can't make instance of class [" +
				className + "], maybe default constructor is not accessible!") ;
		}
		for (Enumeration e = msg.getKeys () ; e.hasMoreElements () ; )
		{
			String fieldName = (String) e.nextElement () ;
			Object value = msg.get (fieldName) ;

			if (value != null)
			try
			{
				java.lang.reflect.Field field = beanClass.getDeclaredField (fieldName) ;
				field.setAccessible (true) ;
				field.set (bean , value) ;
			}
			catch (Throwable t)
			{
				t.printStackTrace (System.err) ;
			}
		}
		return bean ;
	}
	/**
	 * Include a Object packing description in LOPT.
	 */
	private static class PackingDescription
	{
		Class objectClass ;

		Expression mainExp ;
		Expression [] operExp ;

		/**
		 * Constructor.
		 */
		public PackingDescription (Class objectClass ,
			Expression main , Expression [] opers)
		{
			this.objectClass = objectClass ;
			mainExp = main ;
			operExp = opers ;
		}
		/**
		 * Constructor.
		 */
		public PackingDescription (Class objectClass ,
			Expression main , Vector opers)
		{
			this.objectClass = objectClass ;
			mainExp = main ;
			operExp = new Expression [opers.size ()] ;
			for (int i = 0 ; i < operExp.length ; i ++)
				operExp [i] = (Expression) opers.elementAt (i) ;
		}
		/**
		 * Pack object.
		 */
		public Vector pack (Object obj)
			throws JERuntimeException
		{
			Vector v = new Vector () ;

			mainExp.pack (obj , v) ;

			for (int i = 0 ; operExp != null && i < operExp.length ; i ++)
				operExp [i].pack (obj , v) ;

			return v;
		}

		/**
		 * Restore Object.
		 */
		public Object extract (Vector v)
		{

			Object obj = mainExp.getRestoredValue (v) ;

			for (int i = 0 ; operExp != null && i < operExp.length ; i ++)
				operExp [i].getRestoredValue (obj , null , v) ;

			return obj ;

		}

		/**
		 * Using a non-this statements to instanite a object.
		 */
		public Object newInstance ()
		{
			Object obj = mainExp.getReturnValue (null) ;

			for (int i = 0 ; operExp != null && i < operExp.length ; i ++)
				operExp [i].getReturnValue (obj) ;

			return obj ;
		}
	}


	static
	{
		try
		{
			loadDesc () ;
		}
		catch (IOException _ioe)
		{
			throw new JEParserException ("A IOException [" +
				_ioe.getMessage () + "]") ;
		}
	}

	public static synchronized void loadDesc (Reader reader)
		throws IOException , JEParserException
	{
		if (classPackingDefs != null)
			return ;
		reloadDesc (reader) ;
	}

	private static class Mute
	{
		// just for load file.
	}
	/**
	 * Load a file from class path "/conf/lopt.properties".
	 */
	public static synchronized void loadDesc ()
		throws IOException , JEParserException
	{
		InputStream in = new Mute ().getClass ().getResourceAsStream ("/conf/lopt.properties") ;
		if (in == null)
		{
			// throw new IOException ("Configure file not found in classpath [/conf/lopt.properties].") ;
			classPackingDefs = new Hashtable () ;
		}
		if (classPackingDefs != null)
			return ;
		reloadDesc (new InputStreamReader (in , "GBK")) ;
	}

	public static synchronized void reloadDesc (Reader reader)
		throws IOException , JEParserException
	{
		PackingDescriptionLoader loader = new PackingDescriptionLoader (reader) ;

		loader.parse () ;

		classPackingDefs = loader.getDescs () ;
	}

	/**
	 * Load class description from a reader.
	 */
	private static class PackingDescriptionLoader
	{
		int ch = -1 ;
		Reader in = null ;

		Hashtable descs = new Hashtable () ;

		public PackingDescriptionLoader (Reader in)
		{
			this.in = in ;
		}

		/**
		 * Parse class description file.
		 */
		public void parse ()
			throws IOException , JEParserException
		{
			ch = in.read () ;

			peek () ;

			while (ch != -1)
			{
				if (ch == '/')
				{
					maybeComment () ;

					peek () ;

					continue ;
				}

				// get class name
				String className = "" ;
				// System.out.println ("char 1 [" + ((char) ch) + "]") ;
				while (! isWhiteSpaces (ch) && ch != '=')
				{
					className += (char) ch ;
					ch = in.read () ;
				}
				// System.out.println ("class name = " + className) ;
				peek () ;

				if (ch != '=')
					throw new JEParserException ("Must be a '=' after a class name. [" +
						className + ((char) ch) + "]") ;

				Class c = null ;

				try
				{
					c = Class.forName (className) ; // , true , className.getClass ().getClassLoader ()) ;
				}
				catch (Throwable _t)
				{
					// System.out.println ("class error!") ;
					_t.printStackTrace () ;

					// System.out.println ("Exception ok.") ;
					throw new JEParserException ("Class [" + className + "] not found.") ;
				}

				// ch = in.read () ;

				// get main expression.
				JEParser parser = new JEParser (c , in) ;
				ch = parser.parse () ;

				Expression main = parser.getExpression () ;

				peek () ;

				if (ch != ';')
				{
					throw new JEParserException ("Must be a ';' after description. [" + ((char) ch) + "]") ;
				}

				ch = in.read () ;
				peek () ;

				Vector v = new Vector (5) ;
				if (ch == '{')
				{

					while (true)
					{
						// System.out.println ("that char = [" + ((char) ch) + "]" ) ;
						JEParser pe = new JEParser (c , in) ;
						ch = pe.parse () ;

						Expression exp = pe.getExpression () ;

						v.addElement (exp) ;

						peek () ;

						if (ch == '}')
						{
							ch = in.read () ;

							peek () ;
							break ;
						}
						if (ch != ';')
						{
							throw new JEParserException ("Must be a ';' after description. [" + ((char) ch) + "]") ;
						}

					}


				}

				descs.put (c , new PackingDescription (c , main , v)) ;
			}
		}
		public Hashtable getDescs ()
		{
			return descs ;
		}

		/**
		 * Encounter a '//' or '/*'.
		 */
		public void maybeComment ()
			throws IOException , JEParserException
		{
			// System.out.println ("Maybe comment ()") ;
			ch = in.read () ;
			if (ch == '/')
			{
				while ((ch = in.read ()) != '\n' && ch != -1)
					;
				return ;
			}
			else
			if (ch == '*')
			{
				ch = in.read () ;
				while ((ch = in.read ()) != -1)
				{
					if (ch == '*')
						if ((ch = in.read ()) == '/')
						{
							ch = in.read () ;

							return ;
						}
				}
			}
			else
				throw new JEParserException ("Comment format error: [/" +
					((char) ch) + "].") ;
		}

		/**
		 * Is a white spaces defined in
		 * <b>Java Language Specifications for Java 1.2</b>.
		 */
		public static boolean isWhiteSpaces (int ch)
		{
			return (ch == ' ' ||
				ch == '\t' ||
				ch == '\f' ||
				ch == '\n' ||
				ch == '\r') ;
		}

		/**
		 * Ignore white spaces defined in
		 * <b>Java Language Specifications for Java 1.2</b>.
		 */
		public void peek ()
			throws IOException , JEParserException
		{
			while (isWhiteSpaces (ch))
				ch = in.read () ;
		}
	}

	public static void print ()
	{
		System.out.println (classPackingDefs.toString ()) ;
	}
}