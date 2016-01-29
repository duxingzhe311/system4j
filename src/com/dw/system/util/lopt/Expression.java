package com.dw.system.util.lopt ;
// ------------------------------------
// Create at 2001.08.26
// Tab char size : 4 
// ------------------------------------
import java.util.* ;
import java.lang.reflect.* ;

/**
 * A Java Expression, used to pack or restore java Object. <br/>
 * When <code>pack</code> a object, un-resolved value, in general 
 * contains a this reference. will be put into the queue, and the 
 * value will be get from the queue during restoring.<br/>
 * <b>Current Supported: </b><br/>
 * 1. {@link #CONST}, such as a character, string or number. <br/>
 * 2. {@link #METHOD}, a method call. <br/>
 * 3. {@link #FIELD}, a filed evaluation. <br/>
 * 4. {@link #CONSTRUCTOR}, a class instanitation. <br/>
 * 5. {@link #NULL}, null poniter. <br/>
 * 6. {@link #CLASS}, internal type. <br/>
 * 7. {@link #THIS}, this object. <br/>
 * 8. {@link #THAT}, restored object. <br/>
 * 9. {@link #EVALUATE}, a evaluate expression. <br/>
 */
public class Expression
{
	public static final int CONST = 0 ;
	public static final int METHOD = 1 ;
	public static final int FIELD = 2 ;
	public static final int CONSTRUCTOR = 4 ;
	public static final int NULL = 3 ;
	public static final int THIS = 5 ;
	
	public static final int CLASS = 6 ;
	public static final int THAT = 7 ;
	public static final int EVALUATE = 8 ;
	
	int type ;

	Object value ;
	
	Expression member = null ;
	
	Vector args ;
	
	/**
	 * Constructor.
	 */
	public Expression (Object value , int type)
	{
		this.value = value ;
		this.type = type ;
			
		args = new Vector (5) ;
	}
	
	/**
	 * Set a linked expression for this.
	 * That expression will be run by this value.
	 */
	public void setMember (Expression m)
	{
		if (type == NULL || (type == CONST && ! (value instanceof String)))
			throw new RuntimeException ("can't assign a member, parent not a Object!") ;
		if (m.type == CONST || m.type == CONSTRUCTOR || m.type == NULL || m.type == THIS)
			throw new RuntimeException ("can't be a member --> const, constructor, this or null!") ;
		if (type == CLASS)
			if (! Modifier.isStatic (((Member) m.value).getModifiers ()))
				throw new RuntimeException ("Can't call a non-static member on " + value) ;
		
		member = m ;
	}
	
	/**
	 * Add a argument for this expression, must be a 
	 * {@link #METHOD} or {@link #CONSTRUCTOR}.
	 */
	public void addArgument (Expression exp)
	{
		args.addElement (exp) ;
	}
	
	/**
	 * No used for outter use.
	 */
	public Object getValue ()
	{
		return value ;
	}
	
	/**
	 * The type of this expression.
	 */
	public int getType ()
	{
		return type ;
	}
	
	/**
	 * Setter.
	 */
	public void setValue (Object v)
	{
		value = v ;
	}
	
	/** 
	 * Setter.
	 */
	public void setType (int t)
	{
		type = t ;
	}
	
	/**
	 * Set argument for this call.
	 */
	public void setArguments (Vector v)
	{
		args = v ;
	}
	
	/**
	 * Returns arguemnts list.
	 */
	public Vector getArguments ()
	{
		return args ;
	}

	/** 
	 * COunt of arguments.
	 */
	public int getArgumentCount ()
	{
		return args.size () ;
	}
	/** 
	 * Gets argument by index.
	 */
	public Expression getArgument (int index)
	{
		if (args == null)
			return null ;
		return (Expression) (args.elementAt (index)) ;
	}
	/**
	 * Load the member.
	 */
	Expression getMember ()
	{
		return member ;
	}
	
	/**
	 * Convert to string, a scribable text, using a text indent.
	 */
	public String toString (String tabs)
	{
		
		StringBuffer buf = new StringBuffer (100) ;
		
		buf.append (tabs + "TYPE : " + typeToString (type) + "\n") ;
		switch (type)
		{
		case CONSTRUCTOR :
			buf.append (tabs + ((Constructor) value).getDeclaringClass ().getName () + "\n") ;
			break ;
		case METHOD :
			buf.append (tabs + ((Method) value).getName () + " ()\n") ;
			break ;
		case FIELD :
			buf.append (tabs + ((Field) value).getName () + "\n") ;
			break ;
		case THIS :
			buf.append (tabs + "this\n") ;
			break ;
		default :
			buf.append (tabs + value) ;
			break ;
		}
		if (args != null && args.size () > 0)
		{
			buf.append (tabs + "{\n") ; // "ARGUMENTS: -------------------\n") ;
			for (int i = 0 ; i < args.size () ; i ++)
			{
				buf.append (getArgument (i).toString (tabs + "\t") + "\n") ;
				
			}
			buf.append (tabs + "}\n") ;// "END: -------------------\n") ;
		}
		if (member != null)
			buf.append (tabs + "MEMBER : \n" + member.toString (tabs + "\t")) ;
			
		return buf.toString () ;
	}
	/**
	 * To String witjout indent.
	 */
	public String toString ()
	{
		return toString ("") ;
	}
	
	/**
	 * Gets the return value type for this expression.
	 */
	public Class getReturnType ()
	{
		if (member != null)
			return member.getReturnType () ;
		if (type == CONST)
		{
			if (value instanceof String)
				return value.getClass () ;
			if (value instanceof Boolean)
				return Boolean.TYPE ;
			if (value instanceof Byte)
				return Byte.TYPE ;
			if (value instanceof Character)
				return Character.TYPE ;
			if (value instanceof Double)
				return Double.TYPE ;
			if (value instanceof Float)
				return Float.TYPE ;
			if (value instanceof Integer)
				return Integer.TYPE ;
			if (value instanceof Long)
				return Long.TYPE ;
			if (value instanceof Short)
				return Short.TYPE ;
				
			return null ;
		}
		
		if (type == METHOD)
			return ((Method) value).getReturnType () ;
		
		if (type == CONSTRUCTOR)
			return ((Constructor) value).getDeclaringClass () ;
			
		if (type == FIELD)
			return ((Field) value).getType () ;
			
		if (type == CLASS)
			return (Class) value ;
		
		if (type == THIS)
			return (Class) value ;
			
		if (type == THAT)
			return (Class) value ;
			
		if (type == EVALUATE)
			return ((Expression) args.elementAt (0)).getReturnType () ;
		return null ;
	}
	
	/**
	 * Gets the arguemnts' type for this expression, to match method needed.
	 */
	public Class [] getArgumetsTypes ()
	{
		Class [] types = new Class [args.size ()] ;
		
		for (int i = 0 ; i < types.length ; i ++)
		{
			types [i] = getArgument (i).getReturnType () ;
		}
		
		return types ;
	}
	
	/**
	 * Calculate all expression.
	 */
	public Object getReturnValue (Object thisObject)
		throws JERuntimeException
	{
		return getReturnValue (thisObject , null) ;
	}
	
	/** 
	 * Calculate all expression.
	 */
	public Object getReturnValue (Object thisObject , Object obj)
		throws JERuntimeException
	{
		Object rObject = null ;
		try
		{
			switch (type)
			{
			case CONST :
				rObject = value ;
				break ;
			case FIELD :
				rObject = ((Field) value).get (obj) ;
				break ;
			case CONSTRUCTOR :
				rObject = ((Constructor) value).newInstance (getArgumentValues (thisObject)) ;
				break ;
			case METHOD :
				rObject = ((Method) value).invoke (obj , getArgumentValues (thisObject)) ;
				break ;
			case THIS :
				if (member != null)
					return member.getReturnValue (thisObject , thisObject) ;
				else
				return thisObject ;
			case THAT :
				if (member != null)
					return member.getReturnValue (thisObject , thisObject) ;
				else
					return thisObject ;
			case CLASS :
				if (member == null)
					throw new RuntimeException ("Class not allowed here! " + value) ;
				return member.getReturnValue (thisObject , null) ;
			case NULL :
				return null ;
			case EVALUATE : 
				Object ret = ((Expression) args.elementAt (0)).getReturnValue (thisObject , null) ;
				((Expression) args.elementAt (0)).setRuntimeValue (ret , null , thisObject) ;
				return ret ;
			}
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;
			
			throw new RuntimeException ("Error when compute return value!") ;
		}
		if (member != null)
			return member.getReturnValue (thisObject , rObject) ;
		
		return rObject ;
	}
	
	
	/** 
	 * Argument's values.
	 */
	public Object [] getArgumentValues (Object thisObject)
	{
		Object [] values = new Object [args.size ()] ;
		
		for (int i = 0 ; i < args.size () ; i ++)
			values [i] = getArgument (i).getReturnValue (thisObject , null) ;
		
		return values ;
	}
	/**
	 * When restore.
	 */
	public Object [] getRestoredArgumentValues (Object thatObject , Vector v)
	{
		Object [] values = new Object [args.size ()] ;
		
		for (int i = 0 ; i < args.size () ; i ++)
			values [i] = getArgument (i).getRestoredValue (thatObject , null , v) ;
		
		return values ;
	}
	
	/**
	 * Packing a object into vector.
	 */
	public void pack (Object thisObject , Vector v)
		throws JERuntimeException
	{
		pack (thisObject , null , v) ;
	}
	
	/**
	 * Packing a object into vector.
	 */
	public void pack (Object thisObject , Object instance , Vector v)
		throws JERuntimeException
	{
		// System.out.println ("type [" + typeToString (type) + "]") ;
		
		if (type == THIS)
		{
			v.addElement (getReturnValue (thisObject , thisObject)) ;
			return ;
		}
		
		// Object [] argv = getArgumentValues () ;
		
		for (int i = 0 ; i < args.size () ; i ++)
		{
			// System.out.println ("argument type [" + typeToString (getArgument (i).type) + "]") ;
			if (getArgument (i).type == THIS && getArgument (i).getMember () == null)
			{
				v.addElement (getReturnValue (thisObject , thisObject)) ;
				
				return ;
			}
		}
		
		for (int i = 0 ; i < args.size () ; i ++)
		{
			getArgument (i).pack (thisObject , null , v) ;
		}
		
		if (member != null)
		{
			member.pack (thisObject , getReturnValue (thisObject , thisObject) , v) ;
		}
	}
	
	
	/**
	 * Get a restored value or execute a action(for that object).
	 */
	public Object getRestoredValue (Object instance , Vector v)
		throws JERuntimeException
	{
		return getRestoredValue (null , instance , v) ;
	}
	/**
	 * Get a restored value.
	 */
	public Object getRestoredValue (Vector v)
		throws JERuntimeException
	{
		return getRestoredValue (null , null , v) ;
	}
	/**
	 * Get a restored value or execute a action(for that object).
	 */
	public Object getRestoredValue (Object thatObject , Object instance , Vector v)
		throws JERuntimeException
	{
		Object rObject = null ;
		// System.out.println ("type [" + typeToString (type) + "]") ;
		if (type == THIS)
		{
			rObject = v.elementAt (0) ;
			v.removeElementAt (0) ;
			
			return rObject ;
		}
		
		// Object [] argv = getArgumentValues () ;
		
		for (int i = 0 ; i < args.size () ; i ++)
		{
			// System.out.println ("argument type [" + typeToString (getArgument (i).type) + "]") ;
			if (getArgument (i).type == THIS && getArgument (i).getMember () == null)
			{
				rObject = v.elementAt (0) ;
				v.removeElementAt (0) ;
			
				return rObject ;
			}
		}
		
		try
		{
			switch (type)
			{
			case CONST :
				rObject = value ;
				break ;
			case FIELD :
				rObject = ((Field) value).get (instance) ;
				break ;
			case CONSTRUCTOR :
				rObject = ((Constructor) value).newInstance (getRestoredArgumentValues (thatObject , v)) ;
				break ;
			case METHOD :
				rObject = ((Method) value).invoke (instance , getRestoredArgumentValues (thatObject , v)) ;
				break ;
			case CLASS :
				if (member == null)
					throw new RuntimeException ("Class not allowed here! " + value) ;	
				return member.getRestoredValue (thatObject , null , v) ;
			case THAT :
				if (member == null)
					throw new RuntimeException ("Class not allowed here! " + value) ;	
				return member.getRestoredValue (thatObject , thatObject , v) ;
			case EVALUATE :
				Object ret = ((Expression) args.elementAt (1)).getRestoredValue (thatObject , null , v) ;
				((Expression) args.elementAt (0)).setRuntimeValue (ret , null , thatObject) ;
				return ret ;
			case NULL :
				return null ;
			}
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;
			
			throw new RuntimeException ("Error when compute return value!") ;
		}
		
		if (member != null)
			return member.getRestoredValue (thatObject , rObject , v) ;
			
		return rObject ;
	}
	
	
	public void setRuntimeValue (Object v , Object obj , Object thisObject)
	{
		try
		{
			switch (type)
			{
			case CONST :
				value  = v ;
				break ;
			case FIELD :
				((Field) value).set (obj , v) ;
				break ;
			case CONSTRUCTOR :
				// rObject = ((Constructor) value).newInstance (getArgumentValues (thisObject)) ;
				break ;
			case METHOD :
				// rObject = ((Method) value).invoke (obj , getArgumentValues (thisObject)) ;
				break ;
			case THIS :
				if (member != null)
					member.setRuntimeValue (v , thisObject , thisObject) ;
				break ;
			case THAT :
				if (member != null)
					member.setRuntimeValue (v , thisObject , thisObject) ;
				break ;
			case CLASS :
				if (member == null)
					throw new RuntimeException ("Class not allowed here! " + value) ;
				member.setRuntimeValue (v , thisObject , null) ;
			case NULL :
					throw new NullPointerException ("try to evaluate to a null value.") ;
			case EVALUATE :
				break ;
			}
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;
		}
	}
	/** 
	 * Gets a description for type.
	 */
	public static String typeToString (int type)
	{
		switch (type)
		{
		case CONST :
			return "CONST" ;
		case FIELD :
			return "FIELD" ;
		case CONSTRUCTOR :
			return "CONSTRUCTOR" ;
		case METHOD :
			return "METHOD" ;
		case THIS :
			return "THIS" ;
		case NULL :
			return "NULL" ;
		case THAT :
			return "THAT" ;
		case EVALUATE :
			return "EVALUATE" ;
		default :
			return "WRONG" ;
		}
	}
}