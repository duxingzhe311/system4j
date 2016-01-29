package com.dw.system.gdb;

import java.util.*;
import java.sql.*;

import com.dw.system.gdb.conf.DBType;
import com.dw.system.gdb.conf.Func;
import com.dw.system.gdb.connpool.IConnPool;
/**
 * 在线程中的事务
 * 使用方法
 * 
 * try
 * {
 *  GDBTransInThread.create() ;
 *  
 *  ...
 *  //do db access here
 *  ...
 *  
 *  //if it run,rollback will no affect.
 * 	GDBTransInThread.commit() ;
 * }
 * finally
 * {
 * 	 GDBTransInThread.rollback() ;
 * 		
 * }
 * 
 * 
 * @author Jason Zhu
 */
public class GDBTransInThread
{
	private static ThreadLocal<GDBTransInThread> threadTrans = new ThreadLocal<GDBTransInThread>() ;
	
	private static Hashtable<GDBTransInThread,Thread> trans2Th = new Hashtable<GDBTransInThread,Thread>() ;
	/**
	 * 创建线程中事务－－
	 * 
	 * 该方法不允许嵌套
	 * @return
	 */
	public static GDBTransInThread create()
	{
		GDBTransInThread tit = threadTrans.get() ;
		if(tit!=null)
			throw new RuntimeException("Transaction obj is already existed in current thread,may be it has no free before or in nesting!") ;
		
		tit = new GDBTransInThread() ;
		threadTrans.set(tit) ;
		trans2Th.put(tit, Thread.currentThread()) ;
		return tit ;
	}
	
	static GDBTransInThread getTransInThread()
	{
		return threadTrans.get() ;
	}
	
	public static void commit() throws SQLException
	{
		GDBTransInThread tit = threadTrans.get() ;
		if(tit==null)
			return ;//throw new RuntimeException("no Transaction obj found in thread!") ;
		
		if(tit.conn!=null)
		{
			tit.conn.commit() ;
			tit.bCommited = true ;
		}
		//tit.commitConn() ;
		free() ;
	}
	
	/**
	 * 如果已经成功运行commit，则本方法不起作用
	 * @throws SQLException
	 */
	public static void rollback() throws SQLException
	{
		GDBTransInThread tit = threadTrans.get() ;
		if(tit==null)
			return;//throw new RuntimeException("no Transaction obj found in thread!") ;
		
		if(tit.conn!=null)
			tit.conn.rollback() ;
		//tit.rollbackConn() ;
		free() ;
	}
	
	/**
	 * 判断本线程中的事务是否已经释放
	 * @return
	 */
	public static boolean isFreed()
	{
		return getTransInThread()==null ;
	}
	
	/**
	 * 释放事务资源－－也就是数据库连接资源
	 */
	public static void free()
	{
		GDBTransInThread tit = threadTrans.get() ;
		if(tit==null)
			return ;
		
		tit.freeConn() ;
		threadTrans.set(null);
		trans2Th.remove(tit) ;
	}
	
	/**
	 * 获得所有的线程中事务－－该方法支持调试
	 * @return
	 */
	public static GDBTransInThread[] getAllTransInThread()
	{
		synchronized(trans2Th)
		{
			GDBTransInThread[] rets = new GDBTransInThread[trans2Th.size()];
			trans2Th.keySet().toArray(rets) ;
			return rets ;
		}
	}
	
	private IConnPool connPool = null ;
	private Connection conn = null ;
	private boolean bCommited = false;
	
	private boolean oldAutoCommit = true;
	
	private GDBTransInThread()
	{
		
	}
	
	DBType getDBType(Func f)
	{
		if(connPool==null)
			connPool = ConnPoolMgr.getConnPool(f.getRealUsingDBName());
		
		if(connPool==null)
			throw new RuntimeException("no connpool found!") ;
		
		return connPool.getDBType() ;
	}
	
	Connection getConn(String dbname) throws SQLException
	{
		if(conn!=null)
		{//check
			return conn ;
		}
		
		connPool = ConnPoolMgr.getConnPool(dbname);
		if(connPool==null)
			throw new RuntimeException("no connpool found!") ;
		
		conn = connPool.getConnection() ;
		oldAutoCommit = conn.getAutoCommit() ;
		conn.setAutoCommit(false) ;
		return conn ;
	}
	
	private void freeConn()
	{
		if(conn!=null)
		{
			try
			{
				if(!bCommited)
					conn.rollback() ;
				conn.setAutoCommit(oldAutoCommit);
			}
			catch(SQLException ee)
			{}
			connPool.free(conn) ;
			conn = null ;
		}
	}
	
//	private void rollbackConn() throws SQLException
//	{
//		if(conn==null)
//			return ;
//		//if(conn==null)
//		//	throw new RuntimeException("the transcation is freed!") ;
//		
//		try
//		{
//			conn.rollback() ;
//		}
//		finally
//		{
//			free();
//		}
//	}
	
//	private void commitConn() throws SQLException
//	{
//		if(conn==null)
//			return;//	throw new RuntimeException("the transcation is freed!") ;
//		
//		try
//		{
//			conn.commit() ;
//		}
//		finally
//		{
//			free();
//		}
//	}
}
