package com.dw.system.gdb.syn;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.dw.system.gdb.DBUtil;
import com.dw.system.gdb.conf.autofit.JavaColumnInfo;
import com.dw.system.gdb.conf.autofit.JavaTableInfo;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.util.fdb.FDB;

/**
 * 针对一个数据库表的更新所做的Log支持
 * 
 * 数据库某一个表对应的日志存储对象
 * 
 * 数据库表只针对缺省数据库
 * 
 * @author Jason Zhu
 */
public class GDBLogTable
{
	String moduleName = null ;
	String tableName = null ;
	
	//Connection dbConn = null ;
	
	transient Class xormC = null ;
	
	transient String logTableName = null ;
	
	/**
	 * 最大日志记录个数--如果新增时超过记录数，则删除最旧的
	 * -1表示不限
	 */
	private int maxLogNum = -1 ;
	
	/**
	 * 日志存在时间，以毫秒为计算
	 * 如果本值>0,则可以支持如下日志模式——保存固定时间的日志
	 * 如保存7天的日志，可以用来支持Mode2，mode3的表同步
	 *  当在7天之内的变化，可以通过日志进行同步。但超过7天，则需要通过下载全部的数据进行
	 */
	private long logKeptMillisSec = -1 ;
	
	private ArrayList<String> sqlsCreateTable = new ArrayList<String>() ;
	
	private String sqlInsert = null ;
	
	private String sqlCount = null ;
	
	private String sqlCountAfter = null ;
	
	private String sqlListAfter = null ;
	
	private String sqlGetFirst = null ;
	
	private String sqlGetMaxId = null ;
	
	//private String sqlGetFirstIds = null ;
	
	private String sqlGetByDT = null ;
	
	private String sqlDel = null ;
	
	
	GDBLogTable(Class xormc,String modulename,String tablename,int max_logn,long log_kept_ms) throws Exception
	{
		xormC = xormc ;
		moduleName = modulename ;
		tableName = tablename ;
		
		maxLogNum = max_logn;
		logKeptMillisSec = log_kept_ms;
		//dbConn = GDBLogManager.getDerbyConn() ;
		
		logTableName = moduleName+"_"+tablename.toLowerCase() ;
		
		sqlsCreateTable.add("create table "+logTableName+"(log_dt bigint,log_dt_count bigint,sql_str varchar(1000),sql_param blob)");
		sqlsCreateTable.add("CREATE UNIQUE INDEX idx_"+logTableName+" on "+logTableName+
			"(log_dt,log_dt_count)");
		
		sqlInsert = "insert into "+logTableName+"(log_dt,log_dt_count,sql_str,sql_param) values (?,?,?,?)";
		
		sqlCount = "select count(*) from "+logTableName ;
		
		sqlCountAfter = "select count(*) from "+logTableName +" where log_dt>? or (log_dt=? and log_dt_count>?)";
		
		sqlListAfter = "select log_dt,log_dt_count from "+logTableName +" where log_dt>? or (log_dt=? and log_dt_count>?) order by log_dt,log_dt_count";
		
		sqlGetFirst = "select * from "+logTableName +" order by log_dt,log_dt_count";
		
		//sqlGetFirstIds = "select * from "+logTableName +" order by log_dt,log_dt_count";
		sqlGetMaxId = "select max(log_dt) from "+logTableName;
		
		sqlGetByDT = "select * from "+logTableName +" where log_dt=? and log_dt_count=?" ;
		
		sqlDel = "delete from " + logTableName + " where log_dt=? and log_dt_count=?" ;
		
		//检查并创建表
		checkCreateLogTable();
	}
	
	/**
	 * 检查数据库表是否存在，如果不存在，则新建
	 * @throws SQLException 
	 *
	 */
	private synchronized void checkCreateLogTable() throws Exception
	{
		Connection conn = null ;
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			if(DBUtil.tableExists(conn, logTableName))
				return ;
			
			//建立数据库表
			for(String s:sqlsCreateTable)
			{
				System.out.println("sql-="+s) ;
			}
			DBUtil.runSqls(conn, sqlsCreateTable) ;
		}
		finally
		{
			if(conn!=null)
				conn.close() ;
		}
	}
	
	public Class getXormClass()
	{
		return xormC ;
	}
	
	public String getModuleName()
	{
		return moduleName ;
	}
	
	public String getTableName()
	{
		return tableName ;
	}
	
	/**
	 * 获得所有的日志数量
	 * @return
	 * @throws SQLException 
	 */
	public int getLogCount() throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		int c = -1 ;
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlCount);
		
			rs = ps.executeQuery() ;
		
			if(rs.next())
			{
				c = rs.getInt(1) ;
			}
			
			rs.close();
			rs = null ;
			
			ps.close();
			ps = null;
			
			return c ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
	}
	
	
	/**
	 * 获得所有的日志数量
	 * @return
	 * @throws SQLException 
	 */
	public int getLogCountAfter(long dt,long count) throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		int c = -1 ;
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlCountAfter);
			ps.setLong(1, dt) ;
			ps.setLong(2, dt) ;
			ps.setLong(3, count) ;
			
			rs = ps.executeQuery() ;
		
			if(rs.next())
			{
				c = rs.getInt(1) ;
			}
			
			rs.close();
			rs = null ;
			
			ps.close();
			ps = null;
			
			return c ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
	}
	
	public long getMaxLogDT() throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		long c = 0 ;
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlGetMaxId);
		
			rs = ps.executeQuery() ;
		
			if(rs.next())
			{
				c = rs.getLong(1) ;
			}
			
			rs.close();
			rs = null ;
			
			ps.close();
			ps = null;
			
			return c ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
	}
	
	
	/**
	 * 根据时间戳和计数值，列举之后的日志列表
	 * @param dt
	 * @param dt_count
	 * @param max_num 列举的最大值
	 * @return
	 * @throws Exception
	 */
	public ArrayList<long[]> listLogAfter(long dt,long dt_count,int max_num) throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		
		try
		{
			ArrayList<long[]> rets = new ArrayList<long[]>() ;
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlListAfter);
			ps.setLong(1, dt) ;
			ps.setLong(2, dt) ;
			ps.setLong(3, dt_count) ;
			
			rs = ps.executeQuery() ;
		
			int c = 0 ;
			while(rs.next()&&c<max_num)
			{
				long v1 = rs.getLong(1) ;
				long v2 = rs.getLong(2) ;
				
				rets.add(new long[]{v1,v2});
				
				c ++ ;
			}
			
			rs.close();
			rs = null ;
			
			ps.close();
			ps = null;
			
			return rets ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
	}
	
	/**
	 * 根据时间戳和内部计数获得对应的日志项
	 * @param dt
	 * @param dt_count
	 * @return
	 * @throws Exception 
	 */
	public GDBLogItem getLogItem(long dt,long dt_count) throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlGetByDT);
			ps.setLong(1, dt) ;
			ps.setLong(2, dt_count) ;
			
			rs = ps.executeQuery() ;
		
			GDBLogItem ret = null ;
			
			if(rs.next())
			{
				long v1 = rs.getLong("log_dt") ;
				long v2 = rs.getLong("log_dt_count") ;
				String sqlstr = rs.getString("sql_str") ;
				byte[] cont = rs.getBytes("sql_param");
				
				ret = new GDBLogItem(tableName,v1,v2,sqlstr,cont) ;
			}
			
			rs.close();
			rs = null ;
			
			ps.close();
			ps = null;
			
			return ret ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
		
	}
	
	/**
	 * 得到最老的日志记录--
	 * 
	 * @return
	 * @throws Exception 
	 */
	public GDBLogItem getOldestLogItem() throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlGetFirst);
			
			rs = ps.executeQuery() ;
		
			GDBLogItem ret = null ;
			
			if(rs.next())
			{
				long v1 = rs.getLong("log_dt") ;
				long v2 = rs.getLong("log_dt_count") ;
				String sqlstr = rs.getString("sql_str") ;
				byte[] cont = rs.getBytes("sql_param");
				
				ret = new GDBLogItem(tableName,v1,v2,sqlstr,cont) ;
			}
			
			rs.close();
			rs = null ;
			
			ps.close();
			ps = null;
			
			return ret ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
	}
	
	/**
	 * 根据时间戳信息，和计数删除对应的日志
	 * @param dt
	 * @param dt_count
	 * @return
	 * @throws Exception
	 */
	public boolean delLogByDT(long dt,long dt_count) throws Exception
	{
		Connection conn = null ;
		PreparedStatement ps = null;
		boolean oldau = true;
		
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			ps = conn.prepareStatement(sqlDel);
			ps.setLong(1, dt) ;
			ps.setLong(2, dt_count) ;
			
			int r = ps.executeUpdate();
		
			ps.close();
			ps = null;
			
			return r==1 ;
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
				conn.close() ;
		}
		
	}
	
	public void doLog(GDBLogItem li) throws Exception
	{
		int logc = -1;
		ArrayList<long[]> dellogids = null ;
		if(maxLogNum>0)
		{
			logc = getLogCount() ;
			if(logc>=maxLogNum)
			{
				dellogids = listLogAfter(0,0,logc-maxLogNum+1);
			}
		}
		
		Connection conn = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean oldau = true;
		try
		{
			conn = GDBLogManager.getDerbyConn() ;
			oldau = conn.getAutoCommit() ;
			conn.setAutoCommit(false);
			
			ps = conn.prepareStatement(sqlInsert);
		
			ps.setLong(1, li.logDT);
			ps.setLong(2, li.logInDTCount);
			ps.setString(3, li.sqlStr) ;
			
			ps.setBytes(4, li.getLogParamByteArray()) ;
			
			ps.executeUpdate();
			
			ps.close();
			ps = null;
			
			if(dellogids!=null&&dellogids.size()>0)
			{
				ps = conn.prepareStatement(sqlDel);
				for(long[] ids0:dellogids)
				{
					ps.setLong(1, ids0[0]) ;
					ps.setLong(2, ids0[1]) ;
					ps.executeUpdate();
				}
				
				ps.close();
				ps = null;
			}
		
			conn.commit() ;
			conn.setAutoCommit(oldau) ;
			conn.close() ;
			
			conn = null ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
			{
				conn.rollback() ;
				conn.setAutoCommit(oldau) ;
				conn.close() ;
			}
		}
	}
}
