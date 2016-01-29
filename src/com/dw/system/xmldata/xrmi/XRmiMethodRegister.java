package com.dw.system.xmldata.xrmi;

import java.util.*;
import java.lang.reflect.*;

public class XRmiMethodRegister
{
	static Hashtable<String,MethodItem> name2mi = new Hashtable<String,MethodItem>();
	
	public static void registerMethod(String name,Object o,Method m)
	{
		MethodItem mi = new MethodItem();
		mi.name = name ;
		mi.o = o ;
		mi.m = m ;
		if(name==null||name.equals(""))
			mi.name = m.getName();
		
		name2mi.put(mi.name, mi);
	}
	
	public static MethodItem getMethodItemByName(String n)
	{
		return name2mi.get(n);
	}
	
	public static MethodItem[] getAllMethodItem()
	{
		MethodItem[] mis = new MethodItem[name2mi.size()];
		name2mi.values().toArray(mis);
		return mis ;
	}
}


