package com.dw.system.gdb;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.*;
import java.util.List;
import java.util.Locale;

import com.dw.system.gdb.connpool.IConnPool;

public class DBUtil
{
	public static boolean tableExists(Connection conn, String tableName)
		throws SQLException
	{
			DatabaseMetaData dmd = conn.getMetaData() ;
			return tableExists(dmd, tableName) ;
	}
	
	public static boolean tableExistsCaseSensitive(Connection conn, String tableName)
		throws SQLException
	{
			DatabaseMetaData dmd = conn.getMetaData() ;
			return tableExistsCaseSensitive(dmd, tableName) ;
	}
	
	
	/**
	 * 判断表是否存在
	 * @param dbMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static boolean tableExists(DatabaseMetaData dbMetaData, String tableName)
			throws SQLException
	{
		return (tableExistsCaseSensitive(dbMetaData, tableName)
				|| tableExistsCaseSensitive(dbMetaData, tableName
						.toUpperCase(Locale.US)) || tableExistsCaseSensitive(
				dbMetaData, tableName.toLowerCase(Locale.US)));
	}

	/**
	 * 区分大小写判断数据库表是否存在
	 * @param dbMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static boolean tableExistsCaseSensitive(DatabaseMetaData dbMetaData,
			String tableName) throws SQLException
	{
		ResultSet rsTables = dbMetaData.getTables(null, null, tableName, null);
		try
		{
			boolean found = rsTables.next();
			return found;
		}
		finally
		{
			rsTables.close();
		}
	}

	/**
	 * 判断数据库列是否存在
	 * @param dbMetaData
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	public static boolean columnExists(DatabaseMetaData dbMetaData, String tableName,
			String columnName) throws SQLException
	{
		return (columnExistsCaseSensitive(dbMetaData, tableName, columnName)
				|| columnExistsCaseSensitive(dbMetaData, tableName, columnName
						.toUpperCase(Locale.US))
				|| columnExistsCaseSensitive(dbMetaData, tableName, columnName
						.toLowerCase(Locale.US))
				|| columnExistsCaseSensitive(dbMetaData, tableName
						.toUpperCase(Locale.US), columnName)
				|| columnExistsCaseSensitive(dbMetaData, tableName
						.toUpperCase(Locale.US), columnName
						.toUpperCase(Locale.US))
				|| columnExistsCaseSensitive(dbMetaData, tableName
						.toUpperCase(Locale.US), columnName
						.toLowerCase(Locale.US))
				|| columnExistsCaseSensitive(dbMetaData, tableName
						.toLowerCase(Locale.US), columnName)
				|| columnExistsCaseSensitive(dbMetaData, tableName
						.toLowerCase(Locale.US), columnName
						.toUpperCase(Locale.US)) || columnExistsCaseSensitive(
				dbMetaData, tableName.toLowerCase(Locale.US), columnName
						.toLowerCase(Locale.US)));
	}

	/**
	 * 判断数据库表中是否存在列-区分大小写
	 * @param dbMetaData
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	public static boolean columnExistsCaseSensitive(DatabaseMetaData dbMetaData,
			String tableName, String columnName) throws SQLException
	{
		ResultSet rsTables = dbMetaData.getColumns(null, null, tableName,
				columnName);
		try
		{
			boolean found = rsTables.next();
			return found;
		}
		finally
		{
			rsTables.close();
		}
	}
	
	
	public static void runSqls(Connection conn, List<String> sqls)
		throws SQLException
	{
		PreparedStatement ps = null;
		boolean b_autocommit = true;
		try
		{
			b_autocommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			// f.getInParam(uniquekey)
			DBResult dbr = new DBResult();
	
			for (String sql : sqls)
			{
				//System.out.println("install sql:" + sql);
				ps = conn.prepareStatement(sql);
				ps.execute();
	
				ps.close();
				ps = null;
			}
	
			conn.commit();
			conn.setAutoCommit(b_autocommit);
			conn = null;
		}
		finally
		{
			try
			{// 到这里说明运行出错
				if (conn != null)
					conn.rollback();
	
				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				try
				{
					conn.setAutoCommit(b_autocommit);
				}
				catch (Throwable sqle)
				{
				}
			}
	
			
		}
	}
}
