package com.dw.user.sso;

public class ModuleInfo
{
	String name = null ;
	
	String desc = null ;
	
	public ModuleInfo(String n,String d)
	{
		name = n ;
		desc = d ;
	}
	
	public String getName()
	{
		return name ;
	}
	
	public String getDesc()
	{
		return desc ;
	}
}
