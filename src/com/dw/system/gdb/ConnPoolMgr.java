package com.dw.system.gdb;

import java.util.HashMap;

import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.connpool.IConnPool;

public class ConnPoolMgr
{
	static IConnPool defaultConnPool = null ;
	
	static HashMap<String, IConnPool> dbname2pool = new HashMap<String, IConnPool>();
	
	public static IConnPool getDefaultConnPool()
	{
		return defaultConnPool;
	}
	
	public static IConnPool getConnPool(String dbname)
	{
		if(dbname==null||dbname.equals(""))
			return defaultConnPool ;
		
		IConnPool cp = dbname2pool.get(dbname);
		if (cp != null)
			return cp;

		return defaultConnPool;
	}
	
	public static IConnPool getConnPool(Gdb g)
	{
		IConnPool cp = dbname2pool.get(g.usingDBName);
		if (cp != null)
			return cp;

		return defaultConnPool;
	}
}
