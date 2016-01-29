package com.dw.system.gdb.db_idx;

import java.io.*;
import java.util.*;
import java.sql.*;

//import com.dw.system.gdb.datax.db.connpool.*;
import com.dw.system.gdb.ConnPoolMgr;
import com.dw.system.gdb.conf.DBType;
import com.dw.system.gdb.conf.autofit.DbSql;
import com.dw.system.gdb.connpool.DBInfo;
import com.dw.system.gdb.connpool.IConnPool;
import com.dw.system.xmldata.*;

//import org.hsqldb.*;

//import com.dw.databind.*;
//import com.dw.databind.connpool.*;
//import com.dw.workflow.server.ManagerFactory;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class Database// implements IXmlDataable
{
	private static Database defaultDB = null ;
	private static Object locker = new Object();
	
	public static Database getDefaultDatabase() throws Exception
	{
		if(defaultDB!=null)
			return defaultDB ;
		
		synchronized(locker)
		{
			if(defaultDB!=null)
				return defaultDB ;
			
			defaultDB = new Database(ConnPoolMgr.getDefaultConnPool()) ;
			return defaultDB ;
		}
	}
	// public static Database loadFromXmlData(XmlData xd)
	// {
	// Database db = new Database();
	// db.fromXmlData(xd);
	// return db ;
	// }

//	public static Database createDatabase(DBType dbt, String driver,
//			String url, String usern, String userpsw, int conn_initnum,
//			int conn_maxnum)
//	{
//		try
//		{
//			Properties p = new Properties();
//			p.setProperty("db.driver", driver);
//			p.setProperty("db.url", url);
//			p.setProperty("db.username", usern);
//			p.setProperty("db.password", userpsw);
//			p.setProperty("db.initnumber", "" + conn_initnum);
//			p.setProperty("db.maxnumber", "" + conn_maxnum);
//
//			DBInfo dbi = new DBInfo();
//			dbi.dbType = dbt;
//			dbi.connProp = p;
//
//			return new Database(dbi);
//		}
//		catch (Exception ee)
//		{
//			ee.printStackTrace();
//		}
//		return null;
//	}

//	public static Database createLocalFileDatabase(String dbname, File basedir)
//	{
//		if (dbname == null || dbname.equals(""))
//			throw new IllegalArgumentException(
//					"db name cannot be null or empty!");
//
//		if (!basedir.exists())
//			basedir.mkdirs();
//
//		if (!basedir.isDirectory())
//			throw new IllegalArgumentException("base dir is not directory!");
//
//		try
//		{
//			// System.out.println(datadirf.getCanonicalPath());
//			Properties p = new Properties();
//			p.setProperty("db.driver", "org.hsqldb.jdbcDriver");
//			p.setProperty("db.url", "jdbc:hsqldb:file:"
//					+ basedir.getCanonicalPath() + "/" + dbname);
//
//			p.setProperty("db.username", "sa");
//			p.setProperty("db.password", ""); // =zhuzj
//			p.setProperty("db.initnumber", "2");
//			p.setProperty("db.maxnumber", "5");
//
//			DBInfo dbi = new DBInfo();
//			dbi.dbType = DBType.hsql;
//			dbi.connProp = p;
//
//			return new Database(dbi);
//		}
//		catch (Exception ee)
//		{
//			ee.printStackTrace();
//		}
//		return null;
//	}

	// private DBConnectionPool defaultConnPool = null;
	IConnPool defaultConnPool = null;

	private InnerConnProvider connProvider = null;

	// private DBType dbType = DBType.hsql;
	private DbSql dbSql = null;

	private DBInfo dbInfo = null;

	// private Database(){}

//	public Database(DBInfo dbi) throws Exception
//	{
//		initDB(dbi);
//	}
	
	public Database(IConnPool icp)throws Exception
	{
		defaultConnPool = icp ;
		initDB(icp.getDBInfo());
	}

	public DBInfo getDBInfo()
	{
		return dbInfo;
	}

	// public XmlData toXmlData()
	// {
	// XmlData xd = new XmlData();
	//		
	// Properties ps = defaultConnPool.getConnProp() ;
	// for(Enumeration en = ps.propertyNames() ; en.hasMoreElements();)
	// {
	// String pn = (String)en.nextElement() ;
	// String pv = ps.getProperty(pn);
	// xd.setParamValue(pn, pv);
	// }
	//		
	// xd.setParamValue("db_type", dbType.toString());
	//		
	// return xd;
	// }
	//
	// public void fromXmlData(XmlData xd)
	// {
	// DBType dbt = DBType.valueOf(xd.getParamValueStr("db_type")) ;
	// initDBType(dbt) ;
	//		
	// Properties p = new Properties();
	//		
	// for(String tmpn:xd.getParamNames())
	// {
	// p.setProperty(tmpn, xd.getParamValueStr(tmpn));
	// }
	//        
	// try
	// {
	// defaultConnPool = new DefaultConnectionPool(p);
	// connProvider = new InnerConnProvider();
	// }
	// catch(Exception e)
	// {
	// throw new RuntimeException(e.getMessage());
	// }
	// }

	// private Database(DBType dbt)
	// {
	// initDBType(dbt);
	// }

	private void initDB(DBInfo dbi) throws Exception
	{
		dbInfo = dbi;

		dbSql = DbSql.getDbSqlByDBType(dbInfo.dbType);
		

		// defaultConnPool = new DBConnectionPool(dbi.connProp);
		if(defaultConnPool!=null)
			defaultConnPool = ConnPoolMgr.getDefaultConnPool();

		connProvider = new InnerConnProvider();
	}

	public Database(String filepath)
	{
		try
		{
			// System.out.println(datadirf.getCanonicalPath());
			Properties p = new Properties();
			p.setProperty("db.driver", "org.hsqldb.jdbcDriver");
			// p.setProperty("db.url",
			// "jdbc:hsqldb:hsql://localhost:1234/runtime");
			p.setProperty("db.url", "jdbc:hsqldb:file:" + filepath);

			// p.setProperty("db.url", "jdbc:hsqldb:mem:.");
			p.setProperty("db.username", "sa");
			p.setProperty("db.password", ""); // =zhuzj
			p.setProperty("db.initnumber", "2");
			p.setProperty("db.maxnumber", "5");

			DBInfo dbi = new DBInfo();
			dbi.connProp = p;
			dbi.dbType = DBType.hsql;

			initDB(dbi);
		}
		catch (Exception ee)
		{
			ee.printStackTrace();
		}
	}

	
	public IConnPool getConnPool()
	{
		return defaultConnPool;
	}

	public DBType getDBType()
	{
		return dbInfo.dbType;
	}

	public DbSql getDBSql()
	{
		return dbSql;
	}

	public Vector getAllTables() throws SQLException
	{
		Connection conn = null;
		ResultSet result = null;
		try
		{
			conn = defaultConnPool.getConnection();
			String usertables[] = { "TABLE" };
			DatabaseMetaData dMeta = conn.getMetaData();
			result = dMeta.getTables(null, null, null, usertables);
			Vector<String> tables = new Vector<String>();

			// sqlbob@users Added remarks.
			Vector<String> remarks = new Vector<String>();

			while (result.next())
			{
				tables.addElement(result.getString(3));
				remarks.addElement(result.getString(5));
			}
			return tables;
		}
		finally
		{
			if (result != null)
			{
				result.close();
			}
			if (conn != null)
			{
				defaultConnPool.free(conn);
			}
		}
	}

	public boolean isExistedTable(String tablename) throws SQLException
	{
		Connection conn = null;
		ResultSet result = null;
		try
		{
			conn = defaultConnPool.getConnection();
			String usertables[] = { "TABLE" };
			DatabaseMetaData dMeta = conn.getMetaData();
			result = dMeta.getTables(null, null, null, usertables);
			Vector tables = new Vector();

			// sqlbob@users Added remarks.
			Vector remarks = new Vector();

			while (result.next())
			{
				String tn = result.getString(3);
				if (tn.equalsIgnoreCase(tablename))
				{
					return true;
				}
			}
			return false;
		}
		finally
		{
			if (result != null)
			{
				result.close();
			}
			if (conn != null)
			{
				defaultConnPool.free(conn);
			}
		}
	}

	public Connection getConnection() throws SQLException
	{
		return defaultConnPool.getConnection();
		// try
		// {
		// return defaultConnPool.getConnection();
		// }
		// catch (SQLException sqle)
		// {
		// return null;
		// }
	}

	public void freeConnection(Connection conn)
	{
			defaultConnPool.free(conn);
	}

	public class InnerConnProvider
	// implements ConnProvider
	{
		public Connection acquireConn(String name)
		{
			try
			{
				return defaultConnPool.getConnection();
			}
			catch (SQLException sqle)
			{
				return null;
			}
		}

		public void releaseConn(String name, Connection conn)
		{
				defaultConnPool.free(conn);
		}
	}

	public void runNoResultSqls(String[] sqls) throws Exception
	{
		if (sqls == null || sqls.length <= 0)
		{
			return;
		}

		Connection conn = null;
		Statement st = null;
		try
		{
			conn = getConnection();
			st = conn.createStatement();
			for (int i = 0; i < sqls.length; i++)
			{
				if (sqls[i] == null || sqls[i].equals(""))
					continue;

				System.out.println(" exe sql--->" + sqls[i]);
				st.execute(sqls[i]);
			}
		}
		finally
		{
			if (st != null)
			{
				st.close();
			}

			if (conn != null)
			{
				freeConnection(conn);
			}
		}

	}

	public static void runNoResultSqls(Connection conn, String[] sqls)
			throws Exception
	{
		if (sqls == null || sqls.length <= 0)
		{
			return;
		}

		Statement st = null;
		try
		{
			st = conn.createStatement();
			for (int i = 0; i < sqls.length; i++)
			{
				if (sqls[i] == null || sqls[i].equals(""))
					continue;

				System.out.println(" exe sql--->" + sqls[i]);
				st.execute(sqls[i]);
			}
		}
		finally
		{
			if (st != null)
			{
				st.close();
			}
		}

	}

	public void runNoResultSqls(List<String> sqls) throws Exception
	{
		if (sqls == null || sqls.size() <= 0)
		{
			return;
		}

		Connection conn = null;
		Statement st = null;
		try
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement();
			for (String tmps : sqls)
			{
				System.out.println(" exe sql--->" + tmps);
				st.execute(tmps);
			}
			conn.commit();
			conn.setAutoCommit(true);
			st.close();
			st = null;
			freeConnection(conn);
			conn = null;
		}
		finally
		{
			if (st != null)
			{
				st.close();
			}

			if (conn != null)
			{
				conn.rollback();
				conn.setAutoCommit(true);
				freeConnection(conn);
			}
		}

	}

	public void close()
	{
		this.defaultConnPool.close();
	}
}
