package com.dw.system.util.lopt ;

import java.io.* ;
import java.util.* ;

import java.lang.reflect.* ;

import com.dw.system.util.* ;
// import com.css.system.* ;

public class CallerServer
{
	public static Object call (Message msg)
		throws Throwable
	{
		String className = (String) msg.get (0) ;
		String methodName = (String) msg.get (1) ;
		Object [] args = (Object []) msg.get (2) ;
		
		Class c = Class.forName (className) ; // , false , null) ;
		
		if (args == null)
			args = new Object [0] ;
			
		Class [] types = new Class [args.length] ;
		
		for (int i = 0 ; i < types.length ; i ++)
			if (args [i] != null)
			{
				if (args [i] instanceof Boolean)
					types [i] = Boolean.TYPE ;
				else
				if (args [i] instanceof Byte)
					return Byte.TYPE ;
				else
				if (args [i] instanceof Character)
					types [i] = Character.TYPE ;
				else
				if (args [i] instanceof Double)
					types [i] = Double.TYPE ;
				else
				if (args [i] instanceof Float)
					types [i] = Float.TYPE ;
				else
				if (args [i] instanceof Integer)
					types [i] = Integer.TYPE ;
				else
				if (args [i] instanceof Long)
					types [i] = Long.TYPE ;
				else
				if (args [i] instanceof Short)
					types [i] = Short.TYPE ;
				else
					types [i] = args [i].getClass () ;
				
			}
				
	
		Method [] methods = c.getMethods () ;
			
		Method m = null ;
		
		// System.out.println ("type [0] = " + types [0] + " long: " + Long.TYPE) ;
		
		for (int i = 0 ; i < methods.length ; i ++)
		{
			// System.out.println (methods [i].toString ()) ;
			if (! methods [i].getName ().equals (methodName))
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
			throw new RuntimeException ("can't found method [" + className + "." + methodName + "]!") ;
			
		// Log.getLog ("call.log").log (m.toString ()) ;
		if (Modifier.isStatic (m.getModifiers ()))
			return m.invoke (null , args) ;
		else
			return m.invoke (ObjectPackingToolkit.newInstance (className) , args) ;
		
	}
	
}