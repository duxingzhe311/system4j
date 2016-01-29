package com.dw.system.gdb.connpool;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.gdb.conf.DBType;

public abstract class IConnPool
{
	static Vector<IConnPool> ALL_CONN_POOLS = new Vector<IConnPool>();

	static Hashtable<String, IConnPool> NAME2POOL = new Hashtable<String, IConnPool>();

	/**
	 * 
	 * @return
	 */
	public static IConnPool[] getAllConnPools()
	{
		synchronized (ALL_CONN_POOLS)
		{
			IConnPool[] rets = new IConnPool[ALL_CONN_POOLS.size()];
			ALL_CONN_POOLS.toArray(rets);
			return rets;
		}
	}

	/**
	 * @param n
	 * @return
	 */
	public static IConnPool getConnPool(String n)
	{
		return NAME2POOL.get(n);
	}

	/**
	 * 
	 * @param ps
	 */
	public static void printAllPoolInfo(PrintStream ps)
	{
		IConnPool[] dps = getAllConnPools();
		for (IConnPool cp : dps)
		{
			ps.println(cp.toString());
		}
	}
	
	public static void printAllPoolInfo(PrintWriter ps)
	{
		IConnPool[] dps = getAllConnPools();
		for (IConnPool cp : dps)
		{
			ps.println(cp.toString());
		}
	}

	static void setPool(IConnPool cp)
	{
		synchronized (ALL_CONN_POOLS)
		{
			if (!ALL_CONN_POOLS.contains(cp))
				ALL_CONN_POOLS.add(cp);

			NAME2POOL.put(cp.getDBName(), cp);
		}
	}

	static void unsetPool(IConnPool cp)
	{
		synchronized (ALL_CONN_POOLS)
		{
			ALL_CONN_POOLS.remove(cp);
			NAME2POOL.remove(cp.getDBName());
		}
	}

	static ILogger log = LoggerManager.getLogger(DBConnPool.class
			.getCanonicalName());
	
	private Vector<IConnPoolToucher> connPoolLis = new Vector<IConnPoolToucher>();

	public abstract String getDBName();
	
	public abstract DBType getDBType();
	
	protected abstract void init() throws SQLException;
	
	public Connection getConnection() throws SQLException
	{
		init();
		
		Connection conn = getConn();
		if (conn == null)
			return null;

		if (connPoolLis.size() <= 0)
			return conn;

		synchronized (this)
		{
			if (connPoolLis.size() <= 0)
				return conn;

			// 释放第本连接，以便于IConnPoolListener使用
			this.free(conn);

			while (connPoolLis.size() > 0)
			{
				// 提取出Toucher并且回调通知
				IConnPoolToucher cpt = connPoolLis
						.remove(connPoolLis.size() - 1);
				cpt.OnMeBeTouched();
			}
		}

		return getConn();
	}

	public synchronized void putToucher(IConnPoolToucher cpt)
	{
		connPoolLis.add(cpt);
	}
	
	public abstract DBInfo getDBInfo();
	
	protected abstract Connection getConn() throws SQLException;
	
	public abstract void free(Connection connection);// throws SQLException;
	
	public abstract void close();
}
