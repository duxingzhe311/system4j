package com.dw.net.dirqueue;

public class Util
{
	private static String serverURL = null;
	
	private static String serverURLBk = null;
	
	
	private static String idKey = null;
	
	
	public static void setServer(String url,String urlbk,String idk)
	{
		serverURL = url ;
		serverURLBk = urlbk ;
		idKey = idk ;
	}
	
	
	public static String getServerUrl()
	{
		return serverURL ;
	}
	
	public static String getServerUrlBk()
	{
		return serverURLBk ;
	}
	
	public static String getIdKey()
	{
		return idKey ;
	}
}
