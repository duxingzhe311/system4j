package com.dw.system.util ;

import java.util.* ;
/**
 * Data structure for Message.<br/>
 * <b>Not recommand for other use.</b>
 * @version 1.0.1
 */
public final class Data
{
	// String key ;
	/** the value */
	Object value ;
	/** the data type, such as Message.INTEGER ... */
	short type ;
	/**
	 * Constructor, if value is null, type will be ignored.
	 */
	Data (/*String k , */Object v , short t)
	{
		// key = k ;
		value = v ;
		type = t ;

		if (v == null)
			type = Message.NULL ;
	}
	/**
	 * Make Data.
	 */
	static Data createData (Object o)
	{
		if (o == null)
			return new Data (o , Message.NULL) ;
		if (o instanceof Message)
			return new Data (o , Message.MSG) ;
		else
		if (o instanceof Date)
			return new Data (o , Message.DATETIME) ;
		else
		if (o instanceof Integer)
			return new Data (o , Message.INTEGER) ;
		else
		if (o instanceof Float)
			return new Data (o , Message.FLOAT) ;
		else
		if (o instanceof Double)
			return new Data (o , Message.DOUBLE) ;
		else
		if (o instanceof Byte)
			return new Data (o , Message.BYTE) ;
		else
		if (o instanceof Boolean)
			return new Data (o , Message.BOOLEAN) ;
		else
		if (o instanceof Short)
			return new Data (o , Message.SHORT) ;
		else
		if (o instanceof Long)
			return new Data (o , Message.LONG) ;
		else
		if (o instanceof String)
			return new Data (o , Message.STRING) ;
		else
		if (o instanceof Character)
			return new Data (o , Message.CHAR) ;
		else
		if (o instanceof Void)
			return new Data (o , Message.VOID) ;
		if (o instanceof int [])
		{
			return new Data (o , Message.INTEGER_ARRAY) ;
		}
		else
		if (o instanceof Integer [])
		{
			Integer [] Ia = (Integer []) o ;
			int [] ia = new int [Ia.length] ;
			for (int ii = 0 ; ii < Ia.length ; ii ++)
				ia [ii] = Ia [ii].intValue () ;

			return new Data (ia , Message.INTEGER_ARRAY) ;
		}
		else
		if (o instanceof Float [])
		{
			Float [] Fa = (Float []) o ;
			float [] fa = new float [Fa.length] ;
			for (int fi = 0 ; fi < Fa.length ; fi ++)
				fa [fi] = Fa [fi].floatValue () ;

			return new Data (fa , Message.FLOAT_ARRAY) ;
		}
		else
		if (o instanceof Double [])
		{
			Double [] Da = (Double []) o ;
			double [] da = new double [Da.length] ;
			for (int di = 0 ; di < Da.length ; di ++)
				da [di] = Da [di].doubleValue () ;

			return new Data (da , Message.DOUBLE_ARRAY) ;
		}
		else
		if (o instanceof Byte [])
		{
			Byte [] Ba = (Byte []) o ;
			byte [] ba = new byte [Ba.length] ;
			for (int bi = 0 ; bi < Ba.length ; bi ++)
				ba [bi] = Ba [bi].byteValue () ;

			return new Data (ba , Message.BYTE_ARRAY) ;
		}
		else
		/* // currentyly not supported
		if (o instanceof Boolean [])
			return new Data (o , Message.BOOLEAN) ;
		else
		*/
		if (o instanceof Short [])
		{
			Short [] Sa = (Short []) o ;
			short [] sa = new short [Sa.length] ;
			for (int si = 0 ; si < Sa.length ; si ++)
				sa [si] = Sa [si].shortValue () ;

			return new Data (sa , Message.SHORT_ARRAY) ;
		}
		else
		if (o instanceof Long [])
		{
			Long [] Sa = (Long []) o ;
			long [] sa = new long [Sa.length] ;
			for (int si = 0 ; si < Sa.length ; si ++)
				sa [si] = Sa [si].longValue () ;

			return new Data (sa , Message.LONG_ARRAY) ;
		}
		else
		if (o instanceof String [])
		{
			String [] stra = (String []) o ;

			return new Data (stra , Message.STRING_ARRAY) ;
		}
		else
		if (o instanceof int [])
		{
			int [] inta = (int []) o ;

			return new Data (inta , Message.INTEGER_ARRAY) ;
		}
		else
		if (o instanceof float [])
		{
			float [] floata = (float []) o ;

			return new Data (floata , Message.FLOAT_ARRAY) ;
		}
		else
		if (o instanceof double [])
		{
			double [] doublea = (double []) o ;

			return new Data (doublea , Message.DOUBLE_ARRAY) ;
		}
		else
		if (o instanceof byte [])
		{
			byte [] bytea = (byte []) o ;

			return new Data (bytea , Message.BYTE_ARRAY) ;
		}
		else
		if (o instanceof boolean [])
		{
			boolean [] bola = (boolean []) o ;

			return new Data (bola , Message.BOOLEAN_ARRAY) ;
		}
		else
		if (o instanceof Boolean [])
		{
			Boolean [] Sa = (Boolean []) o ;
			boolean [] sa = new boolean [Sa.length] ;
			for (int si = 0 ; si < Sa.length ; si ++)
				sa [si] = Sa [si].booleanValue () ;
			return new Data (o , Message.BOOLEAN_ARRAY) ;
		}
		else
		if (o instanceof char [])
		{
			return new Data (o , Message.CHAR_ARRAY) ;
		}
		else
		if (o instanceof short [])
		{
			short [] shorta = (short []) o ;

			return new Data (shorta , Message.SHORT_ARRAY) ;
		}
		else
		if (o instanceof long [])
		{
			long [] longa = (long []) o ;

			return new Data (longa , Message.LONG_ARRAY) ;
		}
		else
		if (o instanceof Hashtable)
			return new Data (o , Message.HASH_TABLE) ;
		if (o.getClass ().isArray ())
			// throw new IllegalArgumentException ("Unsupported Object array " + o.getClass ()) ;
			return new Data (o , Message.BEAN_ARRAY) ;
		else
		{
			return new Data (o , Message.BEAN) ;
		}
	}

	public Object getValue ()
	{
		return value ;
	}
	public short getType ()
	{
		return type ;
	}

	public String toString ()
	{

		return Data.typeToString (type) + "\t" + value ;
	}

	public static String typeToString (short type)
	{
		switch (type)
		{
		case Message.NULL :
			return "NULL" ;
		case Message.MSG :
			return "MESSAGE" ;
		case Message.DATETIME :
			return "DATATIME" ;
		case Message.BYTE :
			return "BYTE" ;
		case Message.SHORT :
			return "SHORT" ;
		case Message.INTEGER :
			return "INTEGER" ;
		case Message.LONG :
			return "LONG" ;
		case Message.FLOAT :
			return "FLOAT" ;
		case Message.DOUBLE :
			return "DOUBLE" ;
		case Message.BOOLEAN :
			return "BOOLEAN" ;
		case Message.STRING :
			return "STRING" ;
		case Message.BYTE_ARRAY :
			return "BYTE_ARRAY" ;
		case Message.SHORT_ARRAY :
			return "SHORT_ARRAY" ;
		case Message.INTEGER_ARRAY :
			return "INTEGER_ARRAY" ;
		case Message.LONG_ARRAY :
			return "LONG_ARRAY" ;
		case Message.FLOAT_ARRAY :
			return "FLOAT_ARRAY" ;
		case Message.DOUBLE_ARRAY :
			return "DOUBLE_ARRAY" ;
		case Message.STRING_ARRAY :
			return "STRING_ARRAY" ;
		case Message.BEAN :
			return "SIMPLE_BEAN" ;
		case Message.BEAN_ARRAY :
			return "BEAN_ARRAY" ;
		case Message.CHAR :
			return "UNSIGNED_CHAR" ;
		case Message.CHAR_ARRAY :
			return "UNSIGNED_CHAR_ARRAY" ;
		case Message.HASH_TABLE :
			return "HASH_TABLE" ;
		case Message.VOID :
			return "VOID" ;
		default :
			return "UNSUPPROTED" ;
		}
	}
}

