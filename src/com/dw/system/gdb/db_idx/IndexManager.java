package com.dw.system.gdb.db_idx;

import java.io.*;
import java.util.*;
import java.sql.*;

//import org.hsqldb.*;
//import com.dw.system.gdb.datax.db.connpool.*;
import com.dw.system.logger.*;
import com.dw.system.gdb.connpool.IConnPoolToucher;
//import com.dw.databind.*;
//import com.dw.databind.connpool.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: mem relational database implement index!
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class IndexManager implements IConnPoolToucher
{
	// public static final String DATA_DB = "/data_db/netmon";
	// public static final String MGR_DB = "/mgr_db/netmon";

	private static Hashtable<String,Database> path2Db = new Hashtable<String,Database>();

	private static ILogger log = LoggerManager.getLogger(IndexManager.class.getCanonicalName());
	
	private Database db = null;

	private Indexable mon = null;

	private boolean bHasKeyCol = true;
	
	//private Indexable index = null ;

	public IndexManager(String path, Indexable idx) throws Exception
	{
		//db = new Database(path);
		synchronized (path2Db)
		{
			db = (Database) path2Db.get(path);
			if (db == null)
			{
				db = new Database(path);
				path2Db.put(path, db);
			}
		}

		this.mon = idx;
		//setIndexable(index);
		
		db.getConnPool().putToucher(this);
	}

	public IndexManager(Database db, Indexable idx) throws Exception
	{
		this.db = db;
		this.mon = idx;
		//setIndexable(idx);
		db.getConnPool().putToucher(this);
	}
	
	public void OnMeBeTouched()
	{
		try
		{
			setIndexable(mon);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			log.error(e);
		}
	}

	public Database getDatabase()
	{
		return db;
	}

	public void setIndexable(Indexable idx) throws Exception
	{
		

		String kcol = mon.getDBKeyColumn();

		if (kcol == null || kcol.equals(""))
			bHasKeyCol = false;
		else
			bHasKeyCol = true;
		// 创建表(如果表不存在)
		String tn = mon.getDBTableName();
		if (!db.isExistedTable(tn))
		{
			System.out.println("Create Tables [" + tn + "]for :"
					+ mon.getClass().getName());
			String[] sqls = mon.getDBCreationSqls();
			runNoResultSqls(sqls);
		}
	}
	
//	private void setIndexable(Connection conn,Indexable idx) throws Exception
//	{
//		this.mon = idx;
//
//		String kcol = mon.getDBKeyColumn();
//
//		if (kcol == null || kcol.equals(""))
//			bHasKeyCol = false;
//		else
//			bHasKeyCol = true;
//		// 创建表(如果表不存在)
//		String tn = mon.getDBTableName();
//		if (!db.isExistedTable(tn))
//		{
//			System.out.println("Create Tables [" + tn + "]for :"
//					+ mon.getClass().getName());
//			String[] sqls = mon.getDBCreationSqls();
//			Database.runNoResultSqls(conn,sqls);
//		}
//	}

	public void clear() throws Exception
	{
		String sql = "delete from " + mon.getDBTableName();
		runNoResultSqls(new String[] { sql });
	}

	void runNoResultSqls(String[] sqls) throws Exception
	{
		db.runNoResultSqls(sqls);
	}

	private String SQL_getIndexItemByKeyVal()
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col");

		return SQL_getIndexItem(new String[] { mon.getDBKeyColumn() });
	}

	private String SQL_getIndexItem(String[] condcols)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("select ");
		if (bHasKeyCol)
		{
			sqlsb.append(mon.getDBKeyColumn());
			String[] norcols = mon.getDBNorColumns();
			for (int i = 0; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
		}
		else
		{
			String[] norcols = mon.getDBNorColumns();
			sqlsb.append(norcols[0]);
			for (int i = 1; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
		}

		sqlsb.append(" from ").append(mon.getDBTableName());
		if (condcols != null && condcols.length > 0)
		{
			sqlsb.append(" where");
			for (int i = 0; i < condcols.length; i++)
			{
				sqlsb.append(" ");
				if (i > 0)
				{
					sqlsb.append("AND ");

				}
				sqlsb.append(condcols[i]).append("=?");
			}
		}

		return sqlsb.toString();
	}

	private String SQL_getIndexItem(String strwhere, String orderby)
	{
		return SQL_getIndexItem(null, null, strwhere, orderby);
	}

	private String SQL_getIndexItem(String select_limit, String[] norcols,
			String strwhere, String orderby)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("select ");
		if (select_limit != null)
			sqlsb.append(select_limit).append(" ");

		if (bHasKeyCol)
		{
			sqlsb.append(mon.getDBKeyColumn());

			if (norcols == null || norcols.length <= 0)
				norcols = mon.getDBNorColumns();

			for (int i = 0; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
		}
		else
		{
			if (norcols == null || norcols.length <= 0)
				norcols = mon.getDBNorColumns();

			sqlsb.append(norcols[0]);
			for (int i = 1; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
		}

		sqlsb.append(" from ").append(mon.getDBTableName());

		if (strwhere != null && !strwhere.equals(""))
		{
			sqlsb.append(" where ").append(strwhere);

		}
		if (orderby != null && !orderby.equals(""))
		{
			sqlsb.append(" order by ").append(orderby);
		}

		return sqlsb.toString();
	}
	
	private String SQL_getIndexItem(String select_limit, String[] norcols,
			String[] condcols, String orderby)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("select ");
		if (select_limit != null)
			sqlsb.append(select_limit).append(" ");

		if (bHasKeyCol)
		{
			sqlsb.append(mon.getDBKeyColumn());

			if (norcols == null || norcols.length <= 0)
				norcols = mon.getDBNorColumns();

			for (int i = 0; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
		}
		else
		{
			if (norcols == null || norcols.length <= 0)
				norcols = mon.getDBNorColumns();

			sqlsb.append(norcols[0]);
			for (int i = 1; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
		}

		sqlsb.append(" from ").append(mon.getDBTableName());

		if (condcols != null && condcols.length > 0)
		{
			sqlsb.append(" where");
			for (int i = 0; i < condcols.length; i++)
			{
				sqlsb.append(" ");
				if (i > 0)
				{
					sqlsb.append("AND ");

				}
				sqlsb.append(condcols[i]).append("=?");
			}
		}
		
		if (orderby != null && !orderby.equals(""))
		{
			sqlsb.append(" order by ").append(orderby);
		}

		return sqlsb.toString();
	}

	public IndexItem getIndexItemByKeyVal(Object keyv) throws SQLException
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col!");

		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getIndexItemByKeyVal();
			// System.out.println("[readSingleRecord sql]==" + sql);
			stat = conn.prepareStatement(sql);
			stat.setObject(1, keyv);

			ResultSet rs = stat.executeQuery();

			Hashtable value = null;

			IndexItem ii = null;
			if (!rs.next())
			{ // empty
				return null;
			}
			else
			{
				ii = mon.createEmptyIndex();
				// ii.setValueByColumn(mon.getDBKeyColumn(), keyv);
				ii.m_key = keyv;
				ii.setValueByColumn(mon.getDBKeyColumn(), keyv);
				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					Object v = rs.getObject(norcols[i]);
					ii.setValueByColumn(norcols[i], v);
				}
			}

			if (rs.next())
			{
				throw new RuntimeException(
						"Key BindUnit has more than 1 records!");
			}

			return ii;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public IndexItem[] getIndexItemByColVal(String colname, Object v)
			throws SQLException
	{
		return getIndexItemByColVal(new String[] { colname },
				new Object[] { v });
	}

	public IndexItem[] getIndexItemByColVal(String[] condcols, Object[] condvals)
			throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getIndexItem(condcols);
			// System.out.println("[readSingleRecord sql]==" + sql);
			stat = conn.prepareStatement(sql);
			for (int i = 0; i < condcols.length; i++)
			{
				stat.setObject(i + 1, condvals[i]);
			}

			ResultSet rs = stat.executeQuery();

			Vector<IndexItem> tmpv = new Vector<IndexItem>();
			while (rs.next())
			{
				IndexItem ii = mon.createEmptyIndex();
				// ii.setValueByColumn(mon.getDBKeyColumn(),
				// rs.getObject(mon.getDBKeyColumn()));
				if (bHasKeyCol)
				{
					ii.setValueByColumn(mon.getDBKeyColumn(), rs.getObject(mon.getDBKeyColumn()));
				}

				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					Object o = rs.getObject(norcols[i]);
					ii.setValueByColumn(norcols[i], o);
				}

				tmpv.addElement(ii);
			}

			IndexItem[] rets = new IndexItem[tmpv.size()];
			tmpv.toArray(rets);
			return rets;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	private String SQL_removeIndexItem(String[] condcols)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("delete from ").append(mon.getDBTableName());

		if (condcols != null && condcols.length > 0)
		{
			sqlsb.append(" where");
			for (int i = 0; i < condcols.length; i++)
			{
				sqlsb.append(" ");
				if (i > 0)
				{
					sqlsb.append("AND ");

				}
				sqlsb.append(condcols[i]).append("=?");
			}
		}

		return sqlsb.toString();
	}

	public boolean removeIndexItemByCol(String[] condcols, Object[] colvals)
			throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_removeIndexItem(condcols);
			// System.out.println("[readSingleRecord sql]==" + sql);
			stat = conn.prepareStatement(sql);
			for (int i = 0; i < condcols.length; i++)
			{
				stat.setObject(i + 1, colvals[i]);
			}

			return stat.execute();
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}

	}

	public boolean removeIndexItemByKeyVal(Object keyv) throws SQLException
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col!");

		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = "delete from " + mon.getDBTableName() + " where "
					+ mon.getDBKeyColumn() + "=?";
			// System.out.println("[readSingleRecord sql]==" + sql);
			stat = conn.prepareStatement(sql);
			stat.setObject(1, keyv);
			return stat.execute();
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public Vector getIndexItemByCond(String cond) throws SQLException
	{
		return getIndexItemByCond(cond, null);
	}

	public Vector getIndexItemByCond(String cond, String orderby)
			throws SQLException
	{
		return getIndexItemByCond(null, cond, orderby);
	}

	public Vector getIndexItemByCond(String[] norcols, String cond,
			String orderby) throws SQLException
	{
		return getIndexItemByCond(null, norcols, cond, orderby);
	}
	
	
	
	public Vector getIndexItemByCond(int rowidx,int rowcount, String[] norcols,
			String cond, String orderby) throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getIndexItem(null, norcols, cond, orderby);
			System.out.println("IndexManager.getIndexItemByCond="+sql);
			stat = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY);

			stat.setMaxRows(rowidx+rowcount) ;
			
			ResultSet rs = stat.executeQuery();

			Hashtable value = null;

			Vector<IndexItem> v = new Vector<IndexItem>();
			rs.absolute (rowidx) ;
			for (int i = 0 ; rs.next () ; i ++)
			{
				if (rowcount > 0 && i >= rowcount)
					break ;
				
				IndexItem ii = mon.createEmptyIndex();
				// ii.setValueByColumn(mon.getDBKeyColumn(),
				// rs.getObject(mon.getDBKeyColumn()));
				if (bHasKeyCol)
				{
					String kcol = mon.getDBKeyColumn();
					ii.setValueByColumn(kcol, rs.getObject(kcol));
//					if (ii.m_key == null || ii.m_key.equals(""))
//					{
//						continue;
//					}
				}

				if (norcols == null || norcols.length <= 0)
					norcols = mon.getDBNorColumns();

				for (int j = 0; j < norcols.length; j++)
				{
					Object o = rs.getObject(norcols[j]);
					ii.setValueByColumn(norcols[j], o);
				}

				v.addElement(ii);
			}

			return v;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public Vector getIndexItemByCond(String selectlim, String[] norcols,
			String cond, String orderby) throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getIndexItem(selectlim, norcols, cond, orderby);
			System.out.println("IndexManager.getIndexItemByCond="+sql);
			stat = conn.prepareStatement(sql);

			ResultSet rs = stat.executeQuery();

			Hashtable value = null;

			Vector<IndexItem> v = new Vector<IndexItem>();
			while (rs.next())
			{
				IndexItem ii = mon.createEmptyIndex();
				// ii.setValueByColumn(mon.getDBKeyColumn(),
				// rs.getObject(mon.getDBKeyColumn()));
				if (bHasKeyCol)
				{
					String kcol = mon.getDBKeyColumn();
					ii.setValueByColumn(kcol, rs.getObject(kcol));
//					if (ii.m_key == null || ii.m_key.equals(""))
//					{
//						continue;
//					}
				}

				if (norcols == null || norcols.length <= 0)
					norcols = mon.getDBNorColumns();

				for (int i = 0; i < norcols.length; i++)
				{
					Object o = rs.getObject(norcols[i]);
					ii.setValueByColumn(norcols[i], o);
				}

				v.addElement(ii);
			}

			return v;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public Vector getIndexItemByCond(int rowidx,int rowcount,String[] norcols,
			String[] condcols,Object[] condvals, String orderby) throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getIndexItem(null, norcols, condcols, orderby);
			// System.out.println("IndexManager.getIndexItemByCond="+sql);
			stat = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY);
			stat.setMaxRows(rowidx+rowcount);
			
			for(int i = 0 ; i < condcols.length ; i ++)
			{
				stat.setObject(i+1, condvals[i]);
			}

			ResultSet rs = stat.executeQuery();

			Hashtable value = null;

			Vector<IndexItem> v = new Vector<IndexItem>();
			rs.absolute (rowidx) ;
			for (int i = 0 ; rs.next () ; i ++)
			{
				if (rowcount > 0 && i >= rowcount)
					break ;
				
				IndexItem ii = mon.createEmptyIndex();
				// ii.setValueByColumn(mon.getDBKeyColumn(),
				// rs.getObject(mon.getDBKeyColumn()));
				if (bHasKeyCol)
				{
					ii.m_key = rs.getObject(mon.getDBKeyColumn());
					if (ii.m_key == null || ii.m_key.equals(""))
					{
						continue;
					}
				}

				if (norcols == null || norcols.length <= 0)
					norcols = mon.getDBNorColumns();

				for (int j = 0; j < norcols.length; j++)
				{
					Object o = rs.getObject(norcols[j]);
					ii.setValueByColumn(norcols[j], o);
				}

				v.addElement(ii);
			}

			return v;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}
	
	public Vector getIndexItemBySql(String sql) throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			// String sql = SQL_getIndexItem(cond, orderby);
			System.out.println("IndexManager.getIndexItemByCond="+sql);
			stat = conn.prepareStatement(sql);

			ResultSet rs = stat.executeQuery();

			Hashtable value = null;

			Vector<IndexItem> v = new Vector<IndexItem>();
			while (rs.next())
			{
				IndexItem ii = mon.createEmptyIndex();
				// ii.setValueByColumn(mon.getDBKeyColumn(),
				// rs.getObject(mon.getDBKeyColumn()));
				if (bHasKeyCol)
				{
					ii.m_key = rs.getObject(mon.getDBKeyColumn());
					if (ii.m_key == null || ii.m_key.equals(""))
					{
						continue;
					}
				}
				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					Object o = rs.getObject(norcols[i]);
					ii.setValueByColumn(norcols[i], o);
				}

				v.addElement(ii);
			}

			return v;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	private String SQL_getCount(String strwhere)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("select count(*)");
		sqlsb.append(" from ").append(mon.getDBTableName());

		if (strwhere != null && !strwhere.equals(""))
		{
			sqlsb.append(" where ").append(strwhere);

		}
		return sqlsb.toString();
	}

	private String SQL_getCountByCondCols(String[] condcols)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("select count(*)");
		sqlsb.append(" from ").append(mon.getDBTableName());

		if (condcols != null && condcols.length > 0)
		{
			sqlsb.append(" where");
			for (int i = 0; i < condcols.length; i++)
			{
				sqlsb.append(" ");
				if (i > 0)
				{
					sqlsb.append("AND ");

				}
				sqlsb.append(condcols[i]).append("=?");
			}
		}
		return sqlsb.toString();
	}

	/**
	 * 根据条件获得结果个数
	 * 
	 * @param cond
	 * @return
	 * @throws SQLException
	 */
	public int getCountByCond(String cond) throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getCount(cond);
			// System.out.println("IndexManager.getIndexItemByCond="+sql);
			stat = conn.prepareStatement(sql);

			ResultSet rs = stat.executeQuery();
			if (rs.next())
			{
				return rs.getInt(1);
			}

			return 0;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public int getCountByColCond(String[] condcols, Object[] condvals)
			throws SQLException
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sql = SQL_getCountByCondCols(condcols);
			// System.out.println("IndexManager.getIndexItemByCond="+sql);
			stat = conn.prepareStatement(sql);

			for (int i = 0; i < condcols.length; i++)
			{
				stat.setObject(i + 1, condvals[i]);
			}

			ResultSet rs = stat.executeQuery();
			if (rs.next())
			{
				return rs.getInt(1);
			}

			return 0;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	private String SQL_insertIndexItem()
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("insert into ");

		sqlsb.append(mon.getDBTableName());

		sqlsb.append(" (");

		if (bHasKeyCol)
		{
			sqlsb.append(mon.getDBKeyColumn());
			String[] norcols = mon.getDBNorColumns();
			for (int i = 0; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
			sqlsb.append(") values (");
			sqlsb.append("?");
			for (int i = 0; i < norcols.length; i++)
			{
				sqlsb.append(",?");
			}
		}
		else
		{

			String[] norcols = mon.getDBNorColumns();
			sqlsb.append(norcols[0]);
			for (int i = 1; i < norcols.length; i++)
			{
				sqlsb.append(",").append(norcols[i]);
			}
			sqlsb.append(") values (");
			sqlsb.append("?");
			for (int i = 1; i < norcols.length; i++)
			{
				sqlsb.append(",?");
			}
		}

		sqlsb.append(")");
		return sqlsb.toString();
	}

	private String SQL_insertIndexItem(String pk_seq)
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col");

		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("insert into ");

		sqlsb.append(mon.getDBTableName());

		sqlsb.append(" (");
		sqlsb.append(mon.getDBKeyColumn());
		String[] norcols = mon.getDBNorColumns();
		for (int i = 0; i < norcols.length; i++)
		{
			sqlsb.append(",").append(norcols[i]);
		}
		sqlsb.append(") values (");
		sqlsb.append("next value for " + pk_seq);
		for (int i = 0; i < norcols.length; i++)
		{
			sqlsb.append(",?");
		}
		sqlsb.append(")");
		return sqlsb.toString();
	}

	public void insertIndexItem(IndexItem ii) throws SQLException
	{
		if (ii == null)
		{
			return;
		}
		// if (ii.m_key == null)
		// {
		// return;
		// }

		PreparedStatement ps = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sqlstr = SQL_insertIndexItem();

			// System.out.println("[addRecord sql]=="+sqlstr.toString());
			ps = conn.prepareStatement(sqlstr);
			// ps.setObject(1,ii.getValueByColumn(mon.getDBKeyColumn()));
			if (bHasKeyCol)
			{
				if (!mon.isDBKeyAutoCreation())
				{
					ps.setObject(1, ii.getValueByColumn(mon.getDBKeyColumn()));
				}

				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					ps.setObject(i + 2, ii.getValueByColumn(norcols[i]));
				}
			}
			else
			{
				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					ps.setObject(i + 1, ii.getValueByColumn(norcols[i]));
				}
			}
			// ps.setNull(i + 1, dbc.getType().getJDBCType());

			ps.execute();
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}
	
	public Object insertIndexItemWithGetNewId(IndexItem ii) throws SQLException
	{
		if (ii == null)
		{
			return null;
		}
		// if (ii.m_key == null)
		// {
		// return;
		// }

		PreparedStatement ps = null;
		
		ResultSet rs = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sqlstr = SQL_insertIndexItem();

			// System.out.println("[addRecord sql]=="+sqlstr.toString());
			ps = conn.prepareStatement(sqlstr);
			// ps.setObject(1,ii.getValueByColumn(mon.getDBKeyColumn()));
			if (bHasKeyCol)
			{
				if (!mon.isDBKeyAutoCreation())
				{
					ps.setObject(1, ii.getValueByColumn(mon.getDBKeyColumn()));
				}

				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					Object ov = ii.getValueByColumn(norcols[i]) ;
					ps.setObject(i + 2, ov);
				}
			}
			else
			{
				String[] norcols = mon.getDBNorColumns();
				for (int i = 0; i < norcols.length; i++)
				{
					ps.setObject(i + 1, ii.getValueByColumn(norcols[i]));
				}
			}
			// ps.setNull(i + 1, dbc.getType().getJDBCType());

			ps.execute();
			
			ps.close();
			
			String getnewidsql = "select distinct IDENTITY() from "+mon.getDBTableName();
			ps = conn.prepareStatement(getnewidsql);
			rs = ps.executeQuery();
			if (!rs.next())
			{ // empty
				throw new SQLException("Cannot get new id!");
			}
			else
			{
				return rs.getObject(1);
			}
			
		}
		finally
		{
			if(rs!=null)
				rs.close();
			
			if (ps != null)
			{
				ps.close();
			}
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public void insertIndexItem(IndexItem ii, String pk_seq)
			throws SQLException
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col");

		if (ii == null)
		{
			return;
		}

		PreparedStatement ps = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sqlstr = SQL_insertIndexItem(pk_seq);

			// System.out.println("[addRecord sql]==" + sqlstr.toString());
			ps = conn.prepareStatement(sqlstr);
			// ps.setObject(1,ii.getValueByColumn(mon.getDBKeyColumn()));
			// ps.setObject(1, ii.m_key);
			String[] norcols = mon.getDBNorColumns();
			for (int i = 0; i < norcols.length; i++)
			{
				ps.setObject(i + 1, ii.getValueByColumn(norcols[i]));
			}
			// ps.setNull(i + 1, dbc.getType().getJDBCType());

			ps.execute();
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	private String SQL_updateIndexItemByKey()
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col");

		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("update ");
		sqlsb.append(mon.getDBTableName());
		sqlsb.append(" set");
		String[] norcols = mon.getDBNorColumns();
		for (int i = 0; i < norcols.length; i++)
		{
			if (i > 0)
			{
				sqlsb.append(",");

			}
			sqlsb.append(" ").append(norcols[i]).append("=").append("?");
		}
		sqlsb.append(" where ");
		sqlsb.append(mon.getDBKeyColumn()).append("=?");

		return sqlsb.toString();
	}

	public void updateIndexItemByKey(IndexItem ii) throws SQLException
	{
		if (!bHasKeyCol)
			throw new RuntimeException("no key col");

		if (ii == null)
		{
			return;
		}
		if (ii.getValueByColumn(mon.getDBKeyColumn()) == null)
		{
			return;
		}

		PreparedStatement ps = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sqlstr = SQL_updateIndexItemByKey();

			// System.out.println("[update sql]=="+sqlstr.toString());
			ps = conn.prepareStatement(sqlstr);
			String[] norcols = mon.getDBNorColumns();
			int i;
			for (i = 0; i < norcols.length; i++)
			{
				ps.setObject(i + 1, ii.getValueByColumn(norcols[i]));
			}
			// ps.setNull(i + 1, dbc.getType().getJDBCType());
			// set key
			ps.setObject(i + 1, ii.getValueByColumn(mon.getDBKeyColumn()));

			ps.execute();
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	public String SQL_updateColsByCondCols(String[] cols, String[] condcols)
	{
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("update ");
		sqlsb.append(mon.getDBTableName());
		sqlsb.append(" set");

		for (int i = 0; i < cols.length; i++)
		{
			if (i > 0)
			{
				sqlsb.append(",");

			}
			sqlsb.append(" ").append(cols[i]).append("=").append("?");
		}

		if (condcols != null && condcols.length > 0)
		{
			sqlsb.append(" where");
			for (int i = 0; i < condcols.length; i++)
			{
				sqlsb.append(" ");
				if (i > 0)
				{
					sqlsb.append("AND ");

				}
				sqlsb.append(condcols[i]).append("=?");
			}
		}

		return sqlsb.toString();
	}

	public void updateColsByCondCols(String[] cols, Object[] vals,
			String[] condcols, Object[] condvals) throws SQLException
	{

		PreparedStatement ps = null;
		// boolean oldcommit = conn.getAutoCommit();
		Connection conn = null;
		try
		{
			conn = db.getConnection();
			String sqlstr = SQL_updateColsByCondCols(cols, condcols);

			// System.out.println("[update sql]=="+sqlstr.toString());
			ps = conn.prepareStatement(sqlstr);
			int i;
			for (i = 0; i < cols.length; i++)
			{
				ps.setObject(i + 1, vals[i]);
			}

			for (i = 0; i < condcols.length; i++)
			{
				ps.setObject(i + cols.length + 1, condvals[i]);
			}

			ps.execute();
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	/**
	 * 增加一个索引项,如果已经存在则更新
	 * 
	 * @param ii
	 */
	public void addIndexItemByKey(IndexItem ii) throws SQLException
	{
		// Object keyv = ii.getValueByNorColumn(mon.getDBKeyColumn()) ;
		Object keyv = ii.getValueByColumn(mon.getDBKeyColumn());
		if (keyv == null)
		{
			throw new IllegalArgumentException(
					"Index Item has no key column val");
		}

		IndexItem tmpii = getIndexItemByKeyVal(keyv);
		if (tmpii == null)
		{ // insert
			insertIndexItem(ii);
		}
		else
		{ // update
			updateIndexItemByKey(ii);
		}
	}

	public Connection getConnection() throws SQLException
	{
		return db.getConnection();
	}

	public void freeConnection(Connection conn)
	{
		db.freeConnection(conn);
	}

	public Connection getDBConn() throws SQLException
	{
		return db.getConnection();
	}

	public void setDBConn(Connection conn)
	{
		db.freeConnection(conn);
	}

	public static void main(String[] args) throws Throwable
	{
		// Connection conn = defaultConnPool.getConnection();
		// Statement st = conn.createStatement();
		// st.execute("create table mytest(id char(20) primary key,name
		// char(20),age integer,sex char(1))");
		// st.close();
		// defaultConnPool.free(conn);

	}

}