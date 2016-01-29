package com.dw.system.exp ;

import java.io.*;
import java.util.*;

public class ThreeItem
{
	public String oper ;
	public Argument arg1, arg2 ;

	public boolean bracket = false ;

	public ThreeItem ()
	{
		arg1 = new Argument () ;
		arg2 = new Argument () ;
	}

	public boolean equals(Object o)
	{
		if(!(o instanceof ThreeItem))
			return false ;

		ThreeItem ti = (ThreeItem)o ;
		if(!ti.oper.equals(oper))
			return false ;
		if(!ti.arg1.equals(arg1))
			return false ;
		if(!ti.arg2.equals(arg2))
			return false ;

		return true ;
	}

	public int hashCode()
	{
		int o = 0 ,a1 = 0 , a2 = 0 ;
		if(oper!=null)
			o = oper.hashCode() ;
		if(arg1!=null)
			a1 = arg1.hashCode() ;
		if(arg2!=null)
			a2 = arg2.hashCode() ;
		return o+a1+a2 ;
	}

	public String toString (String csp,int len)
	{
		char[] tmpcs = new char[len] ;
		for (int i = 0 ; i < len ; i ++)
			tmpcs[i] = ' ' ;
		String prefix = csp + new String(tmpcs) ;
		StringBuffer tmpsb = new StringBuffer () ;
		tmpsb.append (prefix+"<exp oper=\""+oper+"\">\n") ;
		if (arg1.arg instanceof ThreeItem)
		{
			tmpsb.append (prefix+" <arg1>\n") ;
			tmpsb.append (((ThreeItem)arg1.arg).toString(prefix,len)) ;
			tmpsb.append (prefix+" </arg1>\n") ;
		}
		else
		{
			tmpsb.append (prefix+" <arg1 val=\""+arg1.arg.toString()+"\" type=\""+arg1.type+"\"/>\n") ;
		}
		if (arg2.arg instanceof ThreeItem)
		{
			tmpsb.append (prefix+" <arg2>\n") ;
			tmpsb.append (((ThreeItem)arg2.arg).toString(prefix,len)) ;
			tmpsb.append (prefix+" </arg2>\n") ;
		}
		else
		{
			tmpsb.append (prefix+" <arg2 val=\""+arg2.arg.toString()+"\" type=\""+arg2.type+"\"/>\n") ;
		}
		tmpsb.append (prefix+"</exp>\n") ;
		return tmpsb.toString() ;
	}
}