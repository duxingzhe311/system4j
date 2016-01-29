package com.dw.system.task;

public class TaskParam
{
	String pName = null ;
	String pDesc = null ;
	boolean bNeeded = false ;
	
	public TaskParam(String n,String d,boolean bneed)
	{
		pName = n ;
		pDesc = d ;
		bNeeded = bneed ;
	}
	
	public String getName()
	{
		return pName ;
	}
	
	public String getDesc()
	{
		return pDesc ;
	}
	
	public boolean isNeeded()
	{
		return bNeeded ;
	}
}
