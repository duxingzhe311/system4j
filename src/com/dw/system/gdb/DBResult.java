package com.dw.system.gdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import java.lang.reflect.*;

import com.dw.system.Convert;
import com.dw.system.logger.*;
import com.dw.system.gdb.conf.Func;
import com.dw.system.gdb.conf.ORMap;
import com.dw.system.gdb.xorm.XORMUtil;

public class DBResult
{
	static ILogger log = LoggerManager
			.getLogger(DBResult.class.getCanonicalName());

	// / <summary>
	// / �������޸����ݿ����õĽ������
	// / </summary>
	public static final int CMD = 0;

	// / <summary>
	// / ͨ��SQL��ѯ��䷵�صĽ������
	// / </summary>
	public static final int SQL_QUERY = 1;

	// / <summary>
	// / ͨ��SQL������䷵�صĽ������
	// / </summary>
	public static final int SQL_UPDATE = 2;

	// / <summary>
	// / ͨ��SQLɾ����䷵�صĽ������
	// / </summary>
	public static final int SQL_DELETE = 3;

	// / <summary>
	// / ���ô洢���̲����в�ѯ�ṹ
	// / </summary>
	public static final int PRO_QUERY = 4;

	// / <summary>
	// / ���ô洢���̣���û�в�ѯ���
	// / </summary>
	public static final int PRO_NONQUERY = 5;

	// / <summary>
	// / ������ݿ�����н��������ó�Ա!=null
	// / </summary>
	DataSet resultSet = new DataSet();

	// / <summary>
	// / ͨ��scalar��ʽ���صĽ��
	// / </summary>
	// internal object scalarRes = null ;
	// / <summary>
	// / ����update��delete��䣬�ó�Ա��ʾӰ�������
	// / </summary>
	int rowsAffected = -1;

	// / <summary>
	// / ��Դ洢���̷���ֵ
	// / </summary>
	int proReturnValue = Integer.MIN_VALUE;

	Hashtable outParams = new Hashtable();

	DBResult()
	{

	}

	transient Func func = null;

	DBResult(Func fi)
	{
		func = fi;
	}

	// / <summary>
	// / ������ݿ�������Ӱ�쵽�����������insert��update��䣩
	// / </summary>
	public int getLastRowsAffected()
	{
		return rowsAffected;
	}

	// / <summary>
	// / ������ݿ�������صĽ����
	// / </summary>
	public DataSet getResultSet()
	{
		return resultSet;
	}
	
	
	DataTable appendRowsAffected(int rows_affect)
	{
		DataTable dt = new DataTable("Table");
		dt.addColumn(new DataColumn("RowAffect", Integer.class));
		DataRow dr = dt.createNewRow();
		dr.putValue(0, rows_affect);
		dt.addRow(dr);
		resultSet.addTable(dt);
		return dt;
	}
	
	
	/**
	 * ����һ����ѯ�Ľ���������DataTable
	 * 
	 * 
	 * @param rset
	 * @param index
	 * @param count
	 * @param drf �����Ϊnull����DataTableֻ��һ�����ݽṹ����������κ���
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	DataTable appendResultSet(String tablename,int tableidx,ResultSet rset, int index, int count,IDBSelectCallback cb)
		throws SQLException, Exception
	{
		DataTable dt = transResultSetToDataTable(tablename,tableidx,rset, index, count,cb);
		if(dt!=null)
			resultSet.addTable(dt);
		return dt ;
	}
	
	public static DataTable transResultSetToDataTable(ResultSet rset,String tablename,int index, int count)
		throws SQLException, Exception
	{
		return transResultSetToDataTable(tablename,0,rset, index, count,null) ;
	}
	
	public static void transResultSetToDataTable(ResultSet rset,String tablename,int index, int count,IDBSelectCallback cb)
		throws SQLException, Exception
	{
		transResultSetToDataTable(tablename,0,rset, index, count,cb) ;
	}
	
	static DataTable transResultSetToDataTable(String tablename,int tableidx,ResultSet rset, int index, int count,IDBSelectCallback cb)
			throws SQLException, Exception
	{
		if(Convert.isNullOrEmpty(tablename))
			tablename = "Table" ;
		DataTable dt = new DataTable(tablename);

		if (index < 0)
			index = 0;

		// get Column Names
		ResultSetMetaData meta = rset.getMetaData();

		int col_count = meta.getColumnCount();
		String[] colName = new String[col_count];

		int j;
		for (j = 0; j < col_count; j++)
		{
			colName[j] = meta.getColumnName(j + 1);
			
			DataColumn dc = new DataColumn(colName[j], null,meta.getColumnType(j+1));
			dt.addColumn(dc);
			//dt.addOrCreateColumn(colName[j]);
		}

		if(cb!=null)
		{
			if(!cb.onFindDataTable(tableidx, dt))
				return null;
		}
		
		long time;

		if (count > 0 && index > 0)
		{
			time = System.currentTimeMillis();
			rset.last();
			if (log.isDebugEnabled())
			{
				log.debug("Result Set move to last: "
						+ (System.currentTimeMillis() - time) + "ms.");
			}
			dt.totalCount = rset.getRow();
			if (dt.totalCount <= index)
				return dt;

			time = System.currentTimeMillis();
			if(index==0)
				rset.beforeFirst() ;
			else
				rset.absolute(index);
			if (log.isDebugEnabled())
			{
				log.debug("Result Set move to  absolute [" + index + "]: "
						+ (System.currentTimeMillis() - time) + "ms.");
			}
		}

		for (int i = 0; rset.next(); i++)
		{
			if (count > 0 && i >= count)
				break;

			DataRow dr = dt.createNewRow();

			for (j = 0; j < col_count; j++)
			{
				Object ov = null;
				int ct = meta.getColumnType(j + 1) ;
				String coln = colName[j] ;
				switch (ct)
				{
				case Types.BLOB:
					ov = SqlDataProcessor.processBlob(rset.getBlob(j + 1),
							coln, rset.wasNull());
					break;
				case Types.CLOB:
					ov = SqlDataProcessor.processClob(rset.getClob(j + 1),
							coln, rset.wasNull());
					break;
				case Types.FLOAT:
					//ov = new Float(rset.getFloat(j + 1));
					ov = new Double(rset.getDouble(j + 1));
					break;
				case Types.REAL:
					ov = new Float(rset.getFloat(j + 1));
					break;
				case Types.DOUBLE:
					ov = new Double(rset.getDouble(j + 1));
					break;
				case Types.BIGINT:
					ov = new Long(rset.getLong(j + 1));
					break;
				case Types.INTEGER:
					ov = new Integer(rset.getInt(j + 1));
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					ov = rset.getBigDecimal(j + 1);
					break;
				case Types.SMALLINT:
					ov = new Short(rset.getShort(j + 1));
					break;
				case Types.TINYINT:
					ov = new Byte(rset.getByte(j + 1));
					break;
				case Types.NULL:
					ov = null;
					break;
				case Types.TIME:
					ov = rset.getTime(j + 1);
					break;
				case Types.DATE:
				/*
				 * // ����Oracle9i�ı仯��ͬ��ʹ��TIMESTAMP�Ĵ������� row [j] = rset.getDate
				 * (j + 1) ; break ;
				 */
				case Types.TIMESTAMP:
					ov = rset.getTimestamp(j + 1);
					break;
				case Types.LONGVARBINARY: // trade as blob. Image.
					ov = SqlDataProcessor
							.processLongBytes(rset.getBinaryStream(j + 1),
									colName[j], rset.wasNull());
					break;
				case Types.BINARY:
				case Types.VARBINARY:
					ov = rset.getBytes(j + 1);
					break;
				case Types.BIT:
					ov = rset.getBoolean(j+1);
					break;
				case Types.LONGVARCHAR: // sybase text data type.
				case Types.CHAR:
				case Types.VARCHAR:
				case Types.OTHER:
				default:
					ov = SqlDataProcessor.processString(rset.getString(j + 1),
							colName[j], rset.wasNull());
					break;
				} // end of switch

				dr.putValue(j, ov);
			} // end of for
			
			if(cb!=null)
			{
				if(!cb.onFindDataRow(tableidx, dt, i, dr))
					return null;//����Ҫ��������ֱ�ӷ���
			}
			else
				dt.addRow(dr);
		} // end of for

		if (index <= 0)
		{
			time = System.currentTimeMillis();
			try
			{
				rset.last();
				if (log.isDebugEnabled())
				{
					log.debug("Result Set move to last: "
							+ (System.currentTimeMillis() - time) + "ms.");
				}
				dt.totalCount = rset.getRow();
			}
			catch(Exception ee)
			{
				if(log.isDebugEnabled())
					log.error(ee) ;
			}
		}

		return dt;
	}

	// / <summary>
	// / ��ý�����ĵ�һ����
	// / </summary>
	public DataTable getResultFirstTable()
	{
		return getResultTable(0);
	}
	
	/**
	 * ���ݱ����ƻ�ö�Ӧ�Ľ���������
	 * ������ ��SqlItem�������ļ���ָ��
	 * @param name
	 * @return
	 */
	public DataTable getResultTable(String name)
	{
		if (resultSet == null)
			return null;
		
		return resultSet.getTable(name) ;
	}

	public DataTable getResultTable(int tab_idx)
	{
		if (resultSet == null)
			return null;
		if (resultSet.getTableNum() <= tab_idx)
			return null;
		return resultSet.getTable(tab_idx);
	}

	// / <summary>
	// / ��ȡ�������һ�����еĵ�һ��
	// / </summary>
	public DataRow getResultFirstRow()
	{
		DataTable dt = getResultFirstTable();
		if (dt == null)
			return null;
		if (dt.getRowNum() <= 0)
			return null;
		return dt.getRow(0);
	}

	public List<DataRow> getResultFirstTableRows()
	{
		DataTable dt = getResultFirstTable();
		if (dt == null)
			return null;
		
		return dt.getRows() ;
	}
	// / <summary>
	// / ��ý������һ�����еĵ�һ���еĵ�һ�н������
	// / </summary>
	public Object getResultFirstColumnOfFirstRow()
	{
		DataRow dr = getResultFirstRow();
		if (dr == null)
			return null;
		if (dr.getBelongToTable().getColumnNum() <= 0)
			return null;

		return dr.getValue(0);
	}

	public Number getResultFirstColOfFirstRowNumber()
	{
		Object o = getResultFirstColumnOfFirstRow();
		if (o == null)
			return null;

		if (o instanceof Number)
			return (Number) o;

		if(o instanceof String)
		{//mysql sum() �ķ��ؾ�Ȼ��varchar
			try
			{
				long lv = Long.parseLong((String)o) ;
				return (Long)lv ;
			}
			catch(Exception ee)
			{
				return null ;
			}
		}
		
		return null;
	}

	// / <summary>
	// / ��ô洢���̵ķ���ֵ
	// / </summary>
	public int getProReturnValue()
	{
		return proReturnValue;
	}

	// / <summary>
	// / ��ô洢���̵��ý����󷵻صĲ���
	// / ��������õ��Ǵ洢���̣���!=null,���������û�в���
	// / �����û�е��ô洢���̣���==null
	// / </summary>
	public Hashtable getOutputParam()
	{
		return outParams;
	}

	// / <summary>
	// /
	// / </summary>
	// / <returns></returns>
	public int GetResultType()
	{
		return 1;
	}

	public <T> ArrayList<T> transTable2XORMObjList(int table_idx, Class<T> t)
			throws Exception
	{
		return transTable2XORMObjList(table_idx, t,null) ;
	}
	
	<T> ArrayList<T> transTable2XORMObjList(int table_idx, Class<T> t,DataOut dout)
		throws Exception
	{
		int ts = this.resultSet.tables.size();
		if (ts <= 0 || ts <= table_idx)
			throw new GdbException("No table find in db result at idx="
					+ table_idx);
		
		DataTable dt = resultSet.getTable(table_idx);
		if(dout!=null)
		{
			dout.totalCount = dt.getTotalCount() ;
		}
		return transTable2XORMObjList(t, dt);
	}

	public static <T> ArrayList<T> transTable2XORMObjList(Class<T> t, DataTable dt) throws InstantiationException, IllegalAccessException, Exception
	{
		ArrayList<T> res = new ArrayList<T>();

		for (DataRow dr : dt.getRows())
		{
			T t_ins = t.newInstance();

			XORMUtil.fillXORMObjByDataRow(dr, t_ins);
			res.add(t_ins);
		}

		return res;
	}

	public static <T> T transDataRow2XORMObj(Class<T> t,DataRow dr)
	 throws InstantiationException, IllegalAccessException, Exception
	{
		T t_ins = t.newInstance();

		XORMUtil.fillXORMObjByDataRow(dr, t_ins);
		
		return t_ins ;
	}
	// / <summary>
	// / ����ORMap������������������б��ת��
	// / </summary>
	// / <param name="table_idx"></param>
	// / <param name="t"></param>
	// / <returns></returns>
	public <T> ArrayList<T> transTable2ObjList(int table_idx, Class<T> t)
	throws Exception
	{
		return transTable2ObjList(table_idx, t,null) ;
	}
	
	public <T> ArrayList<T> transTable2ObjList(String table_name, Class<T> t)
	throws Exception
	{
		DataTable dt = resultSet.getTable(table_name);
		if(dt==null)
			throw new GdbException("No table find in db result with name="
					+ table_name);
		return transTable2ObjList(dt, t, null);
	}
	
	<T> ArrayList<T> transTable2ObjList(int table_idx, Class<T> t,DataOut dout)
	throws Exception
	{
		DataTable dt = resultSet.getTable(table_idx);
		if(dt==null)
			throw new GdbException("No table find in db result at idx="
					+ table_idx);
		return transTable2ObjList(dt, t, dout);
	}
	
	<T> ArrayList<T> transTable2ObjList(DataTable dt, Class<T> t,DataOut dout)
			throws Exception
	{
		ORMap sm = func.getBelongTo().getBelongTo().getORMap(
				t.getCanonicalName());
		if (sm == null)
			throw new GdbException("Cannot find SelectMap with type="
					+ t.getCanonicalName());

//		int ts = this.resultSet.tables.size();
//		if (ts <= 0 || ts <= table_idx)
//			throw new GdbException("No table find in db result at idx="
//					+ table_idx);
//
//		DataTable dt = resultSet.getTable(table_idx);
		if(dout!=null)
			dout.totalCount = dt.totalCount ;
		
		ArrayList<T> res = new ArrayList<T>();

		for (DataRow dr : dt.getRows())
		{
			T t_ins = t.newInstance();

			if (fillObjProp(func, t, dr, sm, t_ins))
				res.add(t_ins);
		}

		return res;
	}

	static <T> boolean fillObjProp(Func fi, Class<T> t, DataRow dr, ORMap sm,
			T o) throws Exception
	{
		DataTable dt = dr.getBelongToTable();
		for (Method m : t.getDeclaredMethods())
		{
			String n = m.getName();
			if (!n.startsWith("set"))
				continue;

			n = n.substring(3);
			String coln = sm.getColumnByProperty(n);
			if (coln != null)
			{
				if (dt.getColumn(coln) == null)
					continue;

				Object v = dr.getValue(coln);
				m.setAccessible(true);
				// System.out.println("Invoke method="+m.getName());
				m.invoke(o, new Object[] { v });
				continue;
			}

			String embedc = sm.getEmbedClassByProperty(n);
			if (embedc != null)
			{
				ORMap emborm = fi.getBelongTo().getBelongTo().getORMap(embedc);
				if (emborm == null)
					continue;

				Class tmpc = Class.forName(embedc, true, t.getClassLoader());
				if (tmpc == null)
					continue;

				Object tmpo = tmpc.newInstance();
				if (!fillObjProp(fi, tmpc, dr, emborm, tmpo))
					continue;

				// ���ö�Ӧ��setXXX����
				m.setAccessible(true);
				m.invoke(o, new Object[] { tmpo });
			}
		}

		return true;
	}
	//
	// public List<string> TransColumnToStringList(int table_idx, int
	// column_idx)
	// {
	// if (ResultSet.Tables.Count <= 0 || ResultSet.Tables.Count <= table_idx)
	// throw new DataAccessException("No table find in db result at idx=" +
	// table_idx);
	//
	// DataTable dt = ResultSet.Tables[table_idx];
	// if (dt.Columns.Count <= 0 || dt.Columns.Count <= column_idx)
	// throw new DataAccessException("No column find in db result table[" +
	// table_idx + "][" + column_idx + "]");
	//
	// List<string> al = new List<string>();
	// foreach (DataRow dr in dt.Rows)
	// {
	// al.Add(Convert.ToString(dr[column_idx]));
	// }
	//
	// return al;
	// }
	//
	// public List<int> TransColumnToIntList(int table_idx, int column_idx)
	// {
	// if (ResultSet.Tables.Count <= 0 || ResultSet.Tables.Count <= table_idx)
	// throw new DataAccessException("No table find in db result at idx=" +
	// table_idx);
	//
	// DataTable dt = ResultSet.Tables[table_idx];
	// if (dt.Columns.Count <= 0 || dt.Columns.Count <= column_idx)
	// throw new DataAccessException("No column find in db result table[" +
	// table_idx + "][" + column_idx + "]");
	//
	// List<int> al = new List<int>();
	// foreach (DataRow dr in dt.Rows)
	// {
	// al.Add(Convert.ToInt32(dr[column_idx]));
	// }
	//
	// return al;
	// }
	//
	// public List<short> TransColumnToShortList(int table_idx, int column_idx)
	// {
	// if (ResultSet.Tables.Count <= 0 || ResultSet.Tables.Count <= table_idx)
	// throw new DataAccessException("No table find in db result at idx=" +
	// table_idx);
	//
	// DataTable dt = ResultSet.Tables[table_idx];
	// if (dt.Columns.Count <= 0 || dt.Columns.Count <= column_idx)
	// throw new DataAccessException("No column find in db result table[" +
	// table_idx + "][" + column_idx + "]");
	//
	// List<short> al = new List<short>();
	// foreach (DataRow dr in dt.Rows)
	// {
	// al.Add(Convert.ToInt16(dr[column_idx]));
	// }
	//
	// return al;
	// }
	//
	//
	// public List<long> TransColumnToLongList(int table_idx, int column_idx)
	// {
	// if (ResultSet.Tables.Count <= 0 || ResultSet.Tables.Count <= table_idx)
	// throw new DataAccessException("No table find in db result at idx=" +
	// table_idx);
	//
	// DataTable dt = ResultSet.Tables[table_idx];
	// if (dt.Columns.Count <= 0 || dt.Columns.Count <= column_idx)
	// throw new DataAccessException("No column find in db result table[" +
	// table_idx + "][" + column_idx + "]");
	//
	// List<long> al = new List<long>();
	// foreach (DataRow dr in dt.Rows)
	// {
	// al.Add(Convert.ToInt64(dr[column_idx]));
	// }
	//
	// return al;
	// }
	//
	//
	// public List<bool> TransColumnToBooleanList(int table_idx, int column_idx)
	// {
	// if (ResultSet.Tables.Count <= 0 || ResultSet.Tables.Count <= table_idx)
	// throw new DataAccessException("No table find in db result at idx=" +
	// table_idx);
	//
	// DataTable dt = ResultSet.Tables[table_idx];
	// if (dt.Columns.Count <= 0 || dt.Columns.Count <= column_idx)
	// throw new DataAccessException("No column find in db result table[" +
	// table_idx + "][" + column_idx + "]");
	//
	// List<bool> al = new List<bool>();
	// foreach (DataRow dr in dt.Rows)
	// {
	// al.Add(Convert.ToBoolean(dr[column_idx]));
	// }
	//
	// return al;
	// }
	//
	//
	// public List<DateTime> TransColumnToDateTimeList(int table_idx, int
	// column_idx)
	// {
	// if (ResultSet.Tables.Count <= 0 || ResultSet.Tables.Count <= table_idx)
	// throw new DataAccessException("No table find in db result at idx=" +
	// table_idx);
	//
	// DataTable dt = ResultSet.Tables[table_idx];
	// if (dt.Columns.Count <= 0 || dt.Columns.Count <= column_idx)
	// throw new DataAccessException("No column find in db result table[" +
	// table_idx + "][" + column_idx + "]");
	//
	// List<DateTime> al = new List<DateTime>();
	// foreach (DataRow dr in dt.Rows)
	// {
	// al.Add(Convert.ToDateTime(dr[column_idx]));
	// }
	//
	// return al;
	// }
}

class SqlDataProcessor
{
	public static byte[] processBlob(java.sql.Blob data, String name,
			boolean isNull) throws SQLException, IOException
	{
		if (data == null || isNull)
			return null;
		// Open a stream to read the Blob data
		InputStream l_blobStream = data.getBinaryStream();
		int len = (int) data.length();

		if (len == 0)
			return new byte[0];
		// create a byte buffer to save the Blob data
		byte[] tmpbuf = new byte[len];
		// Read from the Blob data input stream, and write to the buf
		// stream
		for (int i = 0; l_blobStream.read(tmpbuf, i * 100, 100) != -1; i++)
			; // Read from Blob stream

		l_blobStream.close();

		return tmpbuf;
	}

	public static String processClob(java.sql.Clob data, String name,
			boolean isNull) throws SQLException, IOException
	{
		if (data == null || isNull)
			return null;
		int len = (int) data.length();
		if (len == 0)
			return "";
		// Open a stream to read Clob data
		Reader reader = data.getCharacterStream();
		// Holds the Clob data when the Clob stream is being read
		StringBuffer strbuf = new StringBuffer();

		// Read from the Clob stream and write to the stringbuffer
		int l_nchars = 0; // Number of characters read
		char[] l_buffer = new char[100]; // Buffer holding characters being
		// transferred

		while ((l_nchars = reader.read(l_buffer)) != -1)
			// Read from Clob
			strbuf.append(l_buffer, 0, l_nchars); // Write to StringBuffer

		reader.close(); // Close the Clob input stream
		// m_GUI.m_sugArea.append(new String(l_suggestions)); // Display in GUI
		return strbuf.toString();
	}

	public static Boolean processBoolean(boolean data, String name,
			boolean isNull) throws SQLException
	{
		if (isNull)
			return null;
		return new Boolean(data);
	}

	public static Byte processByte(byte data, String name, boolean isNull)
			throws SQLException
	{
		if (isNull)
			return null;
		return new Byte(data);
	}

	public static java.sql.Date processDate(java.sql.Date data, String name,
			boolean isNull) throws SQLException
	{
		if (isNull)
			return null;
		return data;
	}

	public static Double processDouble(double data, String name, boolean isNull)
			throws SQLException
	{
		if (isNull)
			return null;
		return new Double(data);
	}

	public static Float processFloat(float data, String name, boolean isNull)
			throws SQLException
	{
		if (isNull)
			return null;
		return new Float(data);
	}

	public static Integer processInt(int data, String name, boolean isNull)
			throws SQLException
	{
		if (isNull)
			return null;
		return new Integer(data);
	}

	public static Long processLong(long data, String name, boolean isNull)
			throws SQLException
	{
		return new Long(data);
	}

	public static Short processShort(short data, String name, boolean isNull)
			throws SQLException
	{
		if (isNull)
			return null;
		return new Short(data);
	}

	public static String processString(String data, String name, boolean isNull)
			throws SQLException
	{
		return data;
	}

	public static java.sql.Time processTime(java.sql.Time data, String name,
			boolean isNull) throws SQLException
	{
		if (isNull)
			return null;
		return data;
	}

	public static java.sql.Timestamp processTimeStamp(java.sql.Timestamp data,
			String name, boolean isNull) throws SQLException
	{
		if (isNull)
			return null;
		return data;
	}

	public static byte[] processBytes(byte[] data, String name, boolean isNull)
			throws SQLException
	{
		if (isNull)
			return null;

		return data;
	}

	public static byte[] processLongBytes(InputStream in, String name,
			boolean isNull) throws SQLException, IOException
	{
		if (in == null || isNull)
			return null;
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		byte[] tmpbuf = new byte[100];

		// Read from the binary input stream, and write to the buf
		// stream
		int bufLen = -1;

		while ((bufLen = in.read(tmpbuf, 0, 100)) != -1)
			bytesOut.write(tmpbuf, 0, bufLen);

		in.close();

		return bytesOut.toByteArray();
	}
}