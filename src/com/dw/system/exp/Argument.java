package com.dw.system.exp ;

import java.io.*;
import java.util.*;

public class Argument implements Cloneable
{
	public short type = - 1 ;
	public Object arg = null ;

	public boolean equalsType (Argument oarg)
	{
	return type == oarg.type ;
	}

	public boolean equals(Object o)
	{
	if(!(o instanceof Argument))
		return false ;
	Argument a = (Argument)o ;
	if(a.type!=type)
		return false ;
	return a.arg.equals(arg) ;
	}

	public int hashCode()
	{
		int ti = new Short(type).hashCode() ;
		if(arg!=null)
			return ti+arg.hashCode() ;
		else
			return ti ;
	}

	public String toString ()
	{
	return "ARG["+type+","+arg.toString()+"]" ;
	}
}