package com.dw.system.util.lopt ;
// ------------------------------------
// Create at 2001.08.26
// Tab char size : 4
// ------------------------------------
import java.io.* ;
import java.util.* ;
import java.lang.reflect.* ;

/**
 * A Java Expression Parser, for pack and restore java object.
 * The Syntax reference to <b>Java Language Specifications for Java 1.2</b>. <br/>
 * <b>Supportted Syntax: </b><br/>
 * 1. Unicode escape, e.g. '\\u00BF'.<br/>
 * 2. Const char literals, include escape sequence and octal escape, e.g. '\053', '\'' ,<br/>
 * 3. Const string literals.<br/>
 * 4. Limited number literals, octal and hexadecimal integer is not supportted.<br/>
 * 5. Boolean literals, both true and false.<br/>
 * 6. Null pointer, is null.<br/>
 * 7. this reference, is this. And a this reference must start with this.<br/>
 * 8. Class and it's member, e.g. java.lang.String.valueOf (2000). <br/>
 * 9. New word, new java.lang.String ("abcdefg"). <br/>
 * <b>Limitations: </b><br/>
 * 1. All class name must be it's full name.<br/>
 * 2. Operators is not supportted, such as + - * /.<br/>
 * 3. Comments is not supported, both line and block comments.<br/>
 * This class is used by LOPT.<br/>
 * <b>A Example: </b><br/>
 * If you wan to pack and restore a java.lang.Integer object,
 * just write following code: <br/>
 * <b><i>
 * 	new java.lang.Integer (this.intValue ())
 * </i></b><br/>
 * Then your get a {@link Expression} object named "exp" and pack it:<br/>
 * <b><i>
 * 	Integer i = new Integer (1000) ;<br/>
 * 	Vector v = new Vector () ;<br/>
 *  exp.pack (i , v) ;<br/>
 * </i></b><br/>
 * Then in vector v has a element 1000. And you restore it: <br/>
 * <b><i>
 * i = exp.getRestoredValue (v) ;
 * </i></b><br/>
 * i will be 1000.<br/>
 * <b>A more complex example: </b><br/>
 * <b><i>
 * 	HashtableHelper.restore (HashtableHelper.pack (this))
 * </i></b><br/>
 * You will using a class <code>HashtableHelper</code> to
 * pack a <code>Hashtable</code> and restore it. Of cause
 * you may write the correct method to pack and restore Hashtable.<br/>
 * After you restore a Object, maybe you want to call its method with some
 * parameter, so we supply a "that" pointer to indicate the object you restored.
 * A example: <br/>
 * <b><i>
 * new String (this.substring (2))<br/>
 * that.trim () <br/>
 * </i></b><br/>
 * So the trim () will be executed after that be restored.<br/>
 * @see Expression
 */

public class JEParser
{
	private Class thisClass = null ;

	private Reader in = null ;

	// static String tabs = "" ;

	private int ch = -1 ;

	private Expression root = null ;

	/**
	 * Prepare a parser and ready for parsing.<br/>
	 * @param	tc	the Class of 'this', can be null if no this reference.
	 * @param	in	the input stream.
	 */
	public JEParser (Class tc , Reader in)
	{
		this.thisClass = tc ;

		this.in = in ;
	}

	/**
	 * Start parse the input. When a regular expression parsing ended,
	 * it will terminate whether the stream EOF or not.
	 * @reteurn 	the last char it parsed.
	 */
	public int parse ()
		throws IOException , JEParserException
	{
		// System.out.println ("\nParse start!\n") ;
		ch = in.read () ;

		peek () ;

		if (ch == -1)
			return -1 ;

		root = maybeObject () ;

		// System.out.println ("\nParse end!\n") ;
		return ch ;
	}

	/**
	 * Gets the Expression it parsed.
	 */
	public Expression getExpression ()
	{
		return root ;
	}

	/**
	 * Maybe encounters a constructor statement.
	 * There must be a "new " ahead.
	 */
	public Expression maybeConstructor ()
		throws IOException , JEParserException
	{
		String className = "" ;

		if (! isIdentifier (ch) || (ch >= '0' && ch <= '9'))
			throw new JEParserException ("Class name must start by LATIN or _$! [" +
				((char) ch) + "]") ;

		while (ch != -1 && (isIdentifier (ch) || ch == '.'))
		{
			className += (char) ch ;
			ch = in.read () ;
		}


		peek () ;
		if (ch != '(')
			throw new JEParserException ("Constructor must have a (), unexpected char [" +
				((char) ch) + "]") ;


		ch = in.read () ;
		peek () ;


		/*
		if (depend)
			throw new JEParserException ("Constructor not allowed here.") ;
		*/
		Class c = null ;
		try
		{
			c = Class.forName (className) ; // , false , this.getClass ().getClassLoader ()) ;
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;
			throw new JEParserException ("Class [" + className + "] not found.") ;
		}
		Expression exp = new Expression (null , Expression.CONSTRUCTOR) ;

		// ch = in.read () ;

		exp.setArguments (maybeArguments ()) ;

		Class [] types = exp.getArgumetsTypes () ;

		Constructor [] cons = c.getConstructors () ;

		Constructor con = null ;
		for (int i = 0 ; i < cons.length ; i ++)
		{
			Class [] params = cons [i].getParameterTypes () ;
			if (params.length != types.length)
				continue ;
			int j = 0 ;
			for (j = 0 ; j < params.length ; j ++)
			{
				// System.out.println (params [j].isAssignableFrom (types [j])) ;
				if ((types [j] != null && ! params [j].isAssignableFrom (types [j])))
					break ;
			}

			if (j >= params.length)
			{
				con = cons [i] ;
				break ;
			}
		}

		if (con == null)
			throw new JEParserException ("No suitable Constructor for class [" +
				c.getName () + "]") ;

		exp.setValue (con) ;

		// System.out.println ("after Constructor [" + ((char) ch) + "]") ;

		return exp ;
	}

	/**
	 * Maybe encounters a argument list of a method.
	 * There must be a {@link #maybeMethod(Class,String)}
	 * or {@link #maybeConstructor()} ahead.
	 */
	public Vector maybeArguments ()
		throws IOException , JEParserException
	{
		Vector args = new Vector () ;
		peek () ;

		while (ch != ')')
		{
			Expression arg = maybeObject () ;

			if (arg.getReturnType ().equals (Void.TYPE))
				throw new JEParserException ("Can't use a Void as a argument.") ;
			args.addElement (arg) ;

			peek () ;


			if (ch == ',')
			{
				ch = in.read () ;

				peek () ;
			}
			else
			if (ch == ')')
				break ;
			else
			{
				throw new JEParserException ("Error char [" + ((char)ch) + "] following arguemt.") ;
			}
		}

		if (ch == -1)
			throw new JEParserException ("Need a ) to terminate method!") ;
		ch = in.read () ;
		peek () ;

		return args ;
	}

	/**
	 * The first method parse will called.
	 */
	public Expression maybeObject ()
		throws IOException , JEParserException
	{
		/*
		System.out.println (tabs + "maybeObject start!") ;
		tabs +="\t" ;
		*/
		peek () ;

		Expression exp = null ;
		if (ch == '\'')
			exp = maybeChar () ;
		else
		if (ch == '\"')
			exp = maybeString () ;
		else
		if ((ch >= '0' && ch <= '9') || ch == '-')
			exp = maybeNumber () ;
		else // need a indentifier.ind [] {}
			exp = maybeIdentifier (null) ;

		peek () ;


		// work with a linking operation.
		Expression parent = exp ;

		while (ch == '.')
		{
			// System.out.println ("Return type : [" + parent.getReturnType ()+ "]") ;
			if (parent.getReturnType ().isPrimitive ())
				throw new JEParserException ("Want to convert a primitive value into object.") ;

			ch = in.read () ;
			// System.out.println (tabs + "maybe Member start!") ;
			// tabs +="\t" ;

			Expression member = maybeIdentifier (parent.getReturnType ()) ;

			// tabs = tabs.substring (1) ;
			// System.out.println (tabs + "maybe Member end!") ;
			parent.setMember (member) ;

			parent = member ;

			peek () ;

		}

		// evaluate expression
		parent = exp ;

		Expression evExp = null ;
		while (ch == '=')
		{
			// System.out.println ("found '='.") ;
			Expression evaExp = new Expression (null , Expression.EVALUATE) ;
			evaExp.addArgument (parent) ;

			if (evExp == null)
				evExp = evaExp ;

			ch = in.read () ;
			peek () ;

			Expression valueExp = maybeObject () ;

			// System.out.println (valueExp) ;
			evaExp.addArgument (valueExp) ;

			parent = valueExp ;

			peek () ;
		}


		// tabs = tabs.substring (1) ;
		// System.out.println (tabs + "maybeObject end!") ;
		if (evExp != null)
		{
			// System.out.println ("end of = [" + ((char) ch) + "]") ;
			return evExp ;
		}
		return exp ;
	}

	/**
	 * Maybe encounters a Java identifier, e.g. class instanitaion,
	 * method call, or field evaluation.
	 */
	public Expression maybeIdentifier (Class objectClass)
		throws IOException , JEParserException
	{
		// deal with first token end with separator
		// exp = maybeMember (null) ;

		String identifier = "" ;
		while (ch != -1 && isIdentifier (ch))
		{
			identifier += (char) ch ;
			ch = in.read () ;
		}

		if (identifier.length () == 0)
			throw new JEParserException ("Must be a identifier1!") ;

		// is a null?
		if (identifier.equals ("null"))
			return new Expression (null , Expression.NULL) ;

		// is a true?
		if (identifier.equals ("true"))
			return new Expression (new Boolean (true) , Expression.CONST) ;

		// is a true?
		if (identifier.equals ("false"))
			return new Expression (new Boolean (false) , Expression.CONST) ;

		if (identifier.equals ("this"))
			return new Expression (thisClass , Expression.THIS) ;

		if (identifier.equals ("that"))
			return new Expression (thisClass , Expression.THAT) ;
		// is a class instanite?
		if (identifier.equals ("new"))
		{
			peek () ;
			return maybeConstructor () ;
		}
		// maybe a method call or class name, or field name.
		// need know if have next dot operation?

		if (isWhiteSpaces (ch))
			peek () ;

		if (ch == '.' || ch == '=' || ch == '}' || ch == ';')
		{
			// field or classname
			// If we restrict shadow-this-reference,
			// it must be a class name.
			if (objectClass == null)
			{
				// must be a Class name.
				// following a Field ? or a method call?
				String className = identifier ;
				while (true)
				{
					// System.out.println ("2 [" + className + "].") ;
					try
					{
						objectClass = Class.forName (className) ; // , false , null) ;
					}
					catch (Throwable _t) {}

					// we got a class name now.
					// just return it.
					if (objectClass != null)
						return new Expression (objectClass , Expression.CLASS) ;

					if (isWhiteSpaces (ch))
						peek () ;

					if (ch != '.')
						throw new JEParserException ("Unexcepted char1 [" +
							((char) ch) + "] after [" +
							className + "]!") ;

					identifier = "" ;

					ch = in.read () ;

					while (ch != -1 && isIdentifier (ch))
					{
						identifier += (char) ch ;
						ch = in.read () ;
					}
					// System.out.println ("1 [" + identifier + "].") ;
					if (identifier.length () == 0)
						throw new JEParserException ("Must be a identifier!") ;
					className += "." + identifier ;
				}


				// return new Expression (objectClass , Expression.CLASS) ;
			}
			else
			{
				// must be a field of objectClass ;
				Field field = null ;
				try
				{
					field = objectClass.getField (identifier) ;
				}
				catch (Throwable _t)
				{
					_t.printStackTrace () ;
					throw new JEParserException ("No such field [" +
						identifier + "] of class [" + objectClass.getName () + "]") ;
				}

				return new Expression (field , Expression.FIELD) ;
			}
		}
		else
		if (ch == '(')
		{
			// if objectClass is null
			// using this.Object
			return maybeMethod (objectClass , identifier) ;
			// must be a method
			// if (objectClass == null)

		}
		else
		throw new JEParserException ("Unexcepted char [" +
			((char) ch) + "] after [" +
			identifier + "]") ;


	}

	/**
	 * Maybe encounters a method call,
	 * there must be a '(' ahead.
	 */
	public Expression maybeMethod (Class objectClass , String method)
		throws IOException , JEParserException
	{
		if (objectClass == null)
			throw new JEParserException ("shadow-this-reference to a method or field is not allowed, " +
				"please use this." + method + " instead.") ;


		Expression exp = new Expression (null , Expression.METHOD) ;

		ch = in.read () ;

		exp.setArguments (maybeArguments ()) ;

		Class [] types = exp.getArgumetsTypes () ;

		Method [] methods = objectClass.getMethods () ;

		Method m = null ;


		for (int i = 0 ; i < methods.length ; i ++)
		{
			if ((! methods [i].getName ().equals (method)) ||
				(! Modifier.isPublic (methods [i].getModifiers ())))
				continue ;

			Class [] params = methods [i].getParameterTypes () ;
			if (params.length != types.length)
				continue ;

			int j = 0 ;
			for ( ; j < params.length ; j ++)
				if ((types [j] != null && ! params [j].isAssignableFrom (types [j])))
					break ;

			if (j >= params.length)
			{
				m = methods [i] ;
				break ;
			}
		}

		if (m == null)
			throw new JEParserException ("No suitable Method for class [" +
				objectClass.getName () + "." + method + "]") ;

		exp.setValue (m) ;

		return exp ;
	}

	/**
	 * Maybe a const char, must a ' ahead.
	 */
	public Expression maybeChar ()
		throws IOException , JEParserException
	{
		ch = in.read () ;
		char c = parseChar () ;
		if (ch != '\'')
			throw new JEParserException ("Char definination not end properly! [" + (char) ch + "]") ;
		else
			return new Expression (new Character ((char) c) ,
				Expression.CONST) ;
	}

	/**
	 * Deal with a char, include escapes.
	 */
	public char parseChar ()
		throws IOException , JEParserException
	{
		int c = -1 ;
		// ch = in.read () ;

		String chars = "" ;

		if (ch == '\\')
		{
			ch = in.read () ;
			switch (ch)
			{
			case '\'' :
				c = '\'' ;
				ch = in.read () ;
				break ;
			case 'n' :
				c = '\n' ;
				ch = in.read () ;
				break ;
			case 'f' :
				c = '\f' ;
				ch = in.read () ;
				break ;
			case 't' :
				c = '\t' ;
				ch = in.read () ;
				break ;
			case 'r' :
				c = '\r' ;
				ch = in.read () ;
				break ;
			case 'b' :
				c = '\b' ;
				ch = in.read () ;
				break ;
			case '\\' :
				c = '\\' ;
				ch = in.read () ;
				break ;
			case '\"' :
				c = '\"' ;
				ch = in.read () ;
				break ;
			case '0' :
			case '1' :
			case '2' :
			case '3' :
			case '4' :
			case '5' :
			case '6' :
			case '7' :
				// if firset char not a zero, but have two digit chars between 0 and 8,
				// compiler will report a error.
				for (int i = 0 ; i < 3 ; i ++)
				{
					chars += (char) ch ;
					ch = in.read () ;
					if (ch < '0' || ch > '8')
						break ;
				}

				// if the length of oct digits string is 3 and firset digit is not a zero,
				// will report a error.
				if (chars.length () == 3 && chars.charAt (0) != '0')
					throw new JEParserException ("Wrong oct string [\\" + chars + "]") ;

				// ch = in.read () ;
				if (ch < 0)
					throw new JEParserException ("Wrong oct char [\\" + chars + "].") ;
				else
					return (char) Integer.parseInt (chars , 8) ;

			case 'u' :
			case 'U' :
				chars += (char) in.read () ;
				chars += (char) in.read () ;
				chars += (char) in.read () ;
				chars += (char) in.read () ;

				ch = in.read () ;

				if (ch < 0)
					throw new JEParserException ("Wrong unicode char [\\" + chars + "].") ;
				else
					return (char) Integer.parseInt (chars , 16) ;

			default :

				// c = ch ;
				// c = in.read () ;

				throw new JEParserException ("Error escaping char '\\" + ch + "'.") ;
			}

		}
		else
		{
			c = ch ;

			ch = in.read () ;

		}

		return (char) c ;
	}

	/**
	 * Maybe encounters a string, must be a " ahead.
	 */
	public Expression maybeString ()
		throws IOException , JEParserException
	{

		ch = in.read () ;
		StringBuffer buf = new StringBuffer (30) ;
		while (ch != -1 && ch != '\"')
		{
			if (ch == '\\')
			{
				buf.append (parseChar ()) ;
				// throw new JEParserException ("Escaping char not allowed!") ;
			}
			else
			{
				buf.append ((char) ch) ;

				ch = in.read () ;
			}
		}


		if (ch == -1)
			throw new JEParserException ("String definination not end properly!") ;

		ch = in.read () ;

		Expression exp = new Expression (buf.toString () , Expression.CONST) ;


		return exp ;

	}

	/**
	 * Maybe a number, must be a digit ahead.
	 * To identify a type of a Number, because java is a
	 * strict-typing language, use suffix 'L', 'D', 'F'.
	 * Please see <b>Java Language Specifications for Java 1.2</b>.
	 */
	public Expression maybeNumber ()
		throws IOException , JEParserException
	{
		int type = 'I' ;

		boolean dot = false ;


		StringBuffer buf = new StringBuffer (10) ;

		if (ch == '-')
		{
			buf.append ((char) ch) ;
			ch = in.read () ;

			peek () ;
		}

		while (ch != -1)
		{
			if (ch >= '0' && ch <= '9')
			{
				buf.append ((char) ch) ;
			}
			else
			{
				if (ch == '.')
				{
					if (dot)
						throw new JEParserException ("More then one dot in a number!") ;
					dot = true ;
					type = 'D' ;
					buf.append ((char) ch) ;

					ch = in.read () ;
					continue ;

				}
				else
				if (ch == 'l' || ch == 'L')
				{
					if (dot)
						throw new JEParserException ("Can't convert a floating-point into long!") ;
					else
						type = 'L' ;

					ch = in.read () ;
				}
				else
				if (ch == 'D' || ch == 'd')
				{
					type = 'D' ;
					ch = in.read () ;
				}
				else
				if (ch == 'F' || ch == 'f')
				{
					type = 'F' ;

					ch = in.read () ;
				}
				break ;

			}

			ch = in.read () ;
		}

		if (isIdentifier (ch))
			throw new JEParserException ("Unexcepted char following number. [" +
				((char) ch) + "]") ;
		Object value = null ;
		switch (type)
		{
		case 'I' :
			try
			{
				value = new Integer (buf.toString ()) ;
			}
			catch (Throwable _t)
			{
				throw new JEParserException ("Integer format error [" + buf.toString () + "]" ) ;
			}
			break ;
		case 'L' :
			try
			{
				value = new Long (buf.toString ()) ;
			}
			catch (Throwable _t)
			{
				throw new JEParserException ("Long format error [" + buf.toString () + "]" ) ;
			}
			break ;
		case 'F' :
			try
			{
				value = new Float (buf.toString ()) ;
			}
			catch (Throwable _t)
			{
				throw new JEParserException ("Long format error [" + buf.toString () + "]" ) ;
			}
			break ;
		case 'D' :
			try
			{
				value = new Double (buf.toString ()) ;
			}
			catch (Throwable _t)
			{
				throw new JEParserException ("Long format error [" + buf.toString () + "]" ) ;
			}
			break ;
		}

		return new Expression (value , Expression.CONST) ;
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

	/**
	 * Is a valid identifier character defined in
	 * <b>Java Language Specifications for Java 1.2</b>.
	 */
	public static boolean isIdentifier (int ch)
	{
		return (ch == '_' ||
			(ch >= '0' && ch <= '9') ||
			(ch >= 'a' && ch <= 'z') ||
			(ch >= 'A' && ch <= 'Z')) ;
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
	 * Is a separator defined in
	 * <b>Java Language Specifications for Java 1.2</b>.
	 */
	public static boolean isSeparator (int ch)
	{
		return (ch == '(' ||
			ch == ')' ||
			ch == '{' ||
			ch == '}' ||
			ch == '[' ||
			ch == ']' ||
			ch == ';' ||
			ch == ',' ||
			ch == '.') ;
	}

/*
	public static void main (String argv [])
		throws Throwable
	{
		JEParser parser = new JEParser (Class.forName ("com.css.right.DefaultResourceCatalog") ,
			new FileReader ("c.c")) ;

		parser.parse () ;

		Vector v = new Vector () ;

		com.css.right.DefaultResourceCatalog c = new com.css.right.DefaultResourceCatalog (10l , 200l , 100l , "aaa" , "aaa: llll" ,
				new String [] {"xxx" , "yyy"} , null) ;

		System.out.println ("Return value: " + parser.getExpression ().getReturnValue (
			 c , null)) ;

		parser.getExpression ().pack (c , null , v) ;

		System.out.println (v) ;

		System.out.println ("Restor value: " + parser.getExpression ().getRestoredValue (null , v)) ;
		System.out.println (v) ;
	}
*/
}