package com.dw.grid;

public class GridClientInfo
{
	String ip = null ;
	int port = -1 ;
	
	public GridClientInfo(String ip,int p)
	{
		this.ip = ip ;
		this.port = p ;
	}
	
	public String getClientIp()
	{
		return ip ;
	}
	
	public int getClientPort()
	{
		return port ;
	}
	
	public String toString()
	{
		return "client:"+ip+":"+port ;
	}
}
