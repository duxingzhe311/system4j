package com.dw.system.gdb;

import java.io.Writer;
import java.sql.*;
import java.util.*;

import com.dw.system.gdb.conf.autofit.JavaColumnInfo;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.*;

public class DataRow extends HashMap<String,Object> implements IXmlDataable
{
	DataTable belongToDT = null ;
	//HashMap<String,Object> colName2Val = new HashMap<String,Object>() ;
	
	public DataRow()
	{}
	
	DataRow(DataTable dt,HashMap<String,Object> cn2val)
	{
		belongToDT = dt ;
		//colName2Val = cn2val ;
		this.putAll(cn2val);
	}
	
	DataRow(DataTable dt)
	{
		belongToDT = dt ;
	}
	
	public DataTable getBelongToTable()
	{
		return belongToDT ;
	}
	
	public boolean hasColumn(String coln)
	{
		return belongToDT.getColumn(coln)!=null;
	}
	
	public void putValue(String colname,Object ov)
	{
		if(ov!=null)
		{
			if ((ov instanceof java.util.Date)
					&& !(ov instanceof java.sql.Timestamp))
			{//为了适应jdbc访问数据库的要求，把Date改成Timestamp
				ov = new java.sql.Timestamp(((java.util.Date) ov)
						.getTime());
			}
			
			this.put(colname.toUpperCase(), ov);
		}
		else
			this.remove(colname.toUpperCase());
	}
	
	public void putValue(int idx,Object ov)
	{
		String n = belongToDT.getColumnName(idx);
		if(n==null)
			throw new RuntimeException("no coloumn found with index="+idx);
		
		if(ov!=null)
		{
			if ((ov instanceof java.util.Date)
					&& !(ov instanceof java.sql.Timestamp))
			{
				ov = new java.sql.Timestamp(((java.util.Date) ov)
						.getTime());
			}
			
			this.put(n, ov);
		}
		else
			this.remove(n);
	}
	
	public Object getValue(int idx)
	{
		String n = belongToDT.getColumnName(idx);
		if(n==null)
			return null;
		
		return this.get(n);
	}
	
	public Object getValue(String colname)
	{
		if(colname==null)
			return null;
		
		return this.get(colname.toUpperCase());
	}
	
	public String getValueStr(String colname,String defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
			return (String)o ;
		
		return o.toString() ;
	}
	
	public String getValueStr(int idx,String defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
			return (String)o ;
		
		return o.toString() ;
	}
	
	public java.util.Date getValueDate(String colname,java.util.Date defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof java.util.Date)
			return (java.util.Date)o ;
		
		throw new IllegalArgumentException("not boolean column") ;
	}
	
	public java.util.Date getValueDate(int idx,java.util.Date defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof java.util.Date)
			return (java.util.Date)o ;
		
		throw new IllegalArgumentException("not boolean column") ;
	}
	
	public boolean getValueBool(String colname,boolean defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof Boolean)
			return ((Boolean)o).booleanValue() ;
		
		if(o instanceof Number)
		{
			return ((Number)o).intValue()>0 ;
		}
		
		throw new IllegalArgumentException("not boolean column") ;
	}
	
	public boolean getValueBool(int idx,boolean defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof Boolean)
			return ((Boolean)o).booleanValue() ;
		
		if(o instanceof Number)
		{
			return ((Number)o).intValue()>0 ;
		}
		
		throw new IllegalArgumentException("not boolean column") ;
	}
	
	public short getValueInt16(String colname,short defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Short.parseShort((String)o) ;
		}
		
		return ((Number)o).shortValue() ;
	}
	
	public short getValueInt16(int idx,short defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Short.parseShort((String)o) ;
		}
		
		return ((Number)o).shortValue() ;
	}

	public int getValueInt32(String colname,int defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Integer.parseInt((String)o) ;
		}
		
		return ((Number)o).intValue() ;
	}
	
	public int getValueInt32(int idx,int defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Integer.parseInt((String)o) ;
		}
		
		return ((Number)o).intValue() ;
	}
	
	public long getValueInt64(String colname,long defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Long.parseLong((String)o) ;
		}
		
		return ((Number)o).longValue() ;
	}
	
	public long getValueInt64(int idx,long defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Long.parseLong((String)o) ;
		}
		
		return ((Number)o).longValue() ;
	}
	
	public float getValueFloat(String colname,float defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Float.parseFloat((String)o) ;
		}
		
		return ((Number)o).floatValue() ;
	}
	
	public float getValueFloat(int idx,float defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Float.parseFloat((String)o) ;
		}
		
		return ((Number)o).floatValue() ;
	}
	
	public double getValueDouble(String colname,double defv)
	{
		Object o = getValue(colname) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Double.parseDouble((String)o) ;
		}
		
		return ((Number)o).doubleValue() ;
	}
	
	public double getValueDouble(int idx,double defv)
	{
		Object o = getValue(idx) ;
		if(o==null)
			return defv ;
		
		if(o instanceof String)
		{
			return Double.parseDouble((String)o) ;
		}
		
		return ((Number)o).doubleValue() ;
	}
	
	/**
	 * 在更新数据库值时，可能需要的判断是否改变值相同
	 * 其内部不一定是对象相同的情况下，有可能相对于数据库值是相同的
	 * @param odr
	 * @return
	 */
	public boolean checkRowEquals4DB(DataRow odr)
	{
		if(odr==null)
			return false;
		
		for(DataColumn dc : this.belongToDT.getColumns())
		{
			Object thisv = this.getValue(dc.getName()) ;
			Object ov = odr.getValue(dc.getName()) ;
			if(thisv==null)
			{
				if(ov!=null)
					return false;
			}
			else
			{
				if(thisv.equals(ov))
					continue ;
				
				if(ov==null)
					return false;
				
				if(thisv instanceof java.util.Date && ov instanceof java.util.Date)
				{
					//((java.util.Date)thisv).
					//时间存入数据库之后，纳秒内容会被删除，所以只需要秒相同，就认为相同
					long t1 = ((java.util.Date)thisv).getTime()/1000 ;
					long t2 = ((java.util.Date)ov).getTime()/1000;
					if(t1!=t2)
						return false;
				}
			}
		}
		
		return true ;
	}
	/**
	 * 对本行的某一些列进行整数求和
	 * @param cols
	 * @return
	 */
	public long sumColumnsInt64(int[] cols)
	{
		long lv = 0 ;
		for(int c:cols)
		{
			lv += getValueInt64(c,0) ; 
		}
		return lv ;
	}
	
	
	public long sumColumnsInt64(String[] cols)
	{
		long lv = 0 ;
		for(String c:cols)
		{
			lv += getValueInt64(c,0) ; 
		}
		return lv ;
	}
	
	/**
	 * 对本行的某一些列进行浮点数求和
	 * @param cols
	 * @return
	 */
	public double sumColumnsDouble(int[] cols)
	{
		double lv = 0 ;
		for(int c:cols)
		{
			lv += getValueDouble(c,0) ; 
		}
		return lv ;
	}
	
	public double sumColumnsDouble(String[] cols)
	{
		double lv = 0 ;
		for(String c:cols)
		{
			lv += getValueDouble(c,0) ; 
		}
		return lv ;
	}
	////////////////////////////////
	
	
	public boolean containsKey(Object key)
	{
		if(key==null)
			return false;
		return super.containsKey(key.toString().toUpperCase());
	}

	public Object put(String key, Object vv)
	{
		return super.put(key.toUpperCase(), vv);
	}

	public Object remove(Object key)
	{
		return super.remove(key.toString().toUpperCase());
	}


	public Object get(Object key)
	{
		String k = ((String)key).toUpperCase();
		return super.get(k);
	}
	
	/**
	 * 根据输入的XORM类，获得本行能够注入的对应对象
	 * 
	 * 此方法可以用来支持，多表关联查找的结果行中，对每行的使用采用
	 * 多个相关对象作为结果。方便一些页面列表的展示
	 * @param c
	 * @return
	 */
	public Object getInjectedXORMObj(Class c)
		throws Exception
	{
		Object t_ins = c.newInstance();

		XORMUtil.fillXORMObjByDataRow(this, t_ins);
		
		return t_ins ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		for(DataColumn dc:belongToDT.getColumns())
		{
			String n = dc.getName() ;
			Object o = this.getValue(n) ;
			if(o!=null)
				xd.setParamValue(n, o) ;
		}
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		for(DataColumn dc:belongToDT.getColumns())
		{
			String n = dc.getName() ;
			Object ov = xd.getParamValue(n) ;
			if(ov!=null)
			{//适应jdbc数据库访问要求
				if ((ov instanceof java.util.Date)
						&& !(ov instanceof java.sql.Timestamp))
				{
					ov = new java.sql.Timestamp(((java.util.Date) ov)
							.getTime());
				}
				
				this.put(n, ov) ;
			}
		}
	}

	/**
	 * 根据数据库连接和表和相关列，做数据库的插入操作
	 * @param conn
	 * @param tablename
	 * @param colnames
	 * @throws SQLException 
	 */
	public void doInsertDB(Connection conn,String tablename,String[] colnames) throws SQLException
	{
		StringBuilder insertsql = new StringBuilder() ;
		insertsql.append("insert into ").append(tablename);
		StringBuilder lstr = new StringBuilder(),rstr = new StringBuilder() ;
		lstr.append("(").append(colnames[0]) ;
		rstr.append("(?");
		
		for(int i = 1 ; i < colnames.length ; i ++)
		{
			lstr.append(",").append(colnames[i]) ;
			rstr.append(",?") ;
		}
		lstr.append(")");
		rstr.append(")");
		
		insertsql.append(lstr).append("values").append(rstr) ;
		
		PreparedStatement ps = null;
		try
		{
			//System.out.println(insertsql.toString()) ;
			ps = conn.prepareStatement(insertsql.toString()) ;
			for(int i = 0 ; i < colnames.length ; i ++)
			{
				Object tmpo = GDB.prepareObjVal(this.getValue(colnames[i]));
				if (tmpo != null)
				{
					ps.setObject(i+1, tmpo);
				}
				else
				{
					int sqlt = this.belongToDT.getColumn(colnames[i]).getJdbcType();//JavaColumnInfo.Class2SqlType(c)
					if (sqlt == java.sql.Types.BLOB)// for sqlserver driver
						// NullPointer error
						ps.setObject(1 + i, new byte[0]);
					else
						ps.setNull(1 + i, sqlt);
				}
			}
			
			ps.executeUpdate() ;
		}
		finally
		{
			if(ps!=null)
				ps.close() ;
		}
	}
	
	public void doUpdateDB(Connection conn,String tablename,String uniquecol,String[] cols) throws SQLException
	{
		StringBuilder upsql = new StringBuilder() ;
		upsql.append("update ").append(tablename).append(" set ");
		upsql.append(cols[0]).append("=?") ;
		
		for(int i = 1 ; i < cols.length ; i ++)
		{
			upsql.append(",").append(cols[i]).append("=?") ;
		}
		upsql.append(" where ").append(uniquecol).append("=?");
		
		String[] allcols = new String[cols.length+1] ;
		System.arraycopy(cols, 0, allcols, 0, cols.length) ;
		allcols[cols.length] = uniquecol ;
		PreparedStatement ps = null;
		try
		{
			//System.out.println(upsql.toString());
			ps = conn.prepareStatement(upsql.toString()) ;
			for(int i = 0 ; i < allcols.length ; i ++)
			{
				Object tmpo = GDB.prepareObjVal(this.getValue(allcols[i]));
				if (tmpo != null)
				{
					ps.setObject(i+1, tmpo);
				}
				else
				{
					int sqlt = this.belongToDT.getColumn(allcols[i]).getJdbcType();//JavaColumnInfo.Class2SqlType(c)
					if (sqlt == java.sql.Types.BLOB)// for sqlserver driver
						// NullPointer error
						ps.setObject(1 + i, new byte[0]);
					else
						ps.setNull(1 + i, sqlt);
				}
			}
			
			ps.executeUpdate() ;
		}
		finally
		{
			if(ps!=null)
				ps.close() ;
		}
	}
	
	public void doDeleteDB(Connection conn,String tablename,String uniquecol) throws SQLException
	{
		StringBuilder delsql = new StringBuilder() ;
		delsql.append("delete from ").append(tablename);
		delsql.append(" where ").append(uniquecol).append("=?") ;
		
		PreparedStatement ps = null;
		try
		{
			ps = conn.prepareStatement(delsql.toString()) ;
			Object tmpo = GDB.prepareObjVal(this.getValue(uniquecol));
			if(tmpo==null)
				throw new SQLException("no key value found") ;
			ps.setObject(1, tmpo);
			ps.executeUpdate() ;
		}
		finally
		{
			if(ps!=null)
				ps.close() ;
		}
	}
}
