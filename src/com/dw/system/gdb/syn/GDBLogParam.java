package com.dw.system.gdb.syn;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import com.dw.system.xmldata.*;

/**
 * 日志参数
 * @author Jason Zhu
 */
public class GDBLogParam implements IXmlDataable
{
	public static final int T_SET_NULL = 1 ;
	public static final int T_SET_BOOL = 2 ;
	public static final int T_SET_BYTE = 3 ;
	public static final int T_SET_SHORT = 4 ;
	public static final int T_SET_INT = 5 ;
	public static final int T_SET_LONG = 6 ;
	public static final int T_SET_FLOAT = 7 ;
	public static final int T_SET_DOUBLE = 8 ;
	public static final int T_SET_BIGD = 9 ;
	public static final int T_SET_STR = 10 ;
	public static final int T_SET_BYTES = 11 ;
	public static final int T_SET_DATE = 12 ;
	public static final int T_SET_TIME = 13 ;
	public static final int T_SET_TIMESTAMP = 14 ;
	public static final int T_SET_OBJ = 15 ;
	public static final int T_SET_URL = 16 ;
	//public static final int T_SET_ROWID = 17 ;
	public static final int T_SET_NSTR = 18 ;
	
	int idx = -1 ;
	int paramSetType = -1 ;
	Object paramSetValue = null ;
	int sqlType = Integer.MIN_VALUE ;
	
	public GDBLogParam()
	{}
	
	public GDBLogParam(int idx,int pst,Object psv)
	{
		this.idx = idx ;
		this.paramSetType = pst ;
		this.paramSetValue = psv ;
	}
	
	public GDBLogParam(int idx,int pst,Object psv,int sqlt)
	{
		this.idx = idx ;
		this.paramSetType = pst ;
		this.paramSetValue = psv ;
		this.sqlType = sqlt ;
	}
	
	public int getIdx()
	{
		return idx ;
	}
	
	public int getParamSetType()
	{
		return paramSetType ;
	}
	
	
	
	//private Object recoverValue
	
	public Object getParamSetValue()
	{
		return paramSetValue ;
	}
	
	public int getSqlType()
	{
		return sqlType ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		xd.setParamValue("idx", idx) ;
		xd.setParamValue("t", paramSetType) ;
		if(paramSetValue!=null)
			xd.setParamValue("v", paramSetValue) ;
		if(sqlType!=Integer.MIN_VALUE)
			xd.setParamValue("st", sqlType) ;
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		idx = xd.getParamValueInt32("idx", -1) ;
		paramSetType = xd.getParamValueInt32("t",-1) ;
		paramSetValue = xd.getParamValue("v") ;
		sqlType = xd.getParamValueInt32("st", Integer.MIN_VALUE) ;
	}
	
	
	/**
	 * 当用日志进行更新数据库时，需要调用的方法
	 * @param ps
	 * @throws SQLException 
	 * @throws MalformedURLException 
	 */
	public void setToStatement(PreparedStatement ps) throws SQLException, MalformedURLException
	{
		switch(paramSetType)
		{
		case T_SET_NULL:
			ps.setNull(idx, sqlType);
			break ;
		case T_SET_BOOL:
			ps.setBoolean(idx, (Boolean)paramSetValue) ;
			break ;
		case T_SET_BYTE:
			ps.setByte(idx, (Byte)paramSetValue) ;
			break ;
		case T_SET_SHORT:
			ps.setShort(idx, (Short)paramSetValue) ;
			break ;
		case T_SET_INT:
			ps.setInt(idx, (Integer)paramSetValue) ;
			break ;
		case T_SET_LONG:
			ps.setLong(idx, (Long)paramSetValue) ;
			break ;
		case T_SET_FLOAT:
			ps.setFloat(idx, (Float)paramSetValue) ;
			break ;
		case T_SET_DOUBLE:
			ps.setDouble(idx, (Double)paramSetValue) ;
			break ;
		case T_SET_BIGD:
			ps.setBigDecimal(idx, (BigDecimal)paramSetValue);
			break ;
		case T_SET_STR:
			ps.setString(idx, (String)paramSetValue);
			break ;
		case T_SET_BYTES:
			ps.setBytes(idx, (byte[])paramSetValue);
			break ;
		case T_SET_DATE:
			ps.setDate(idx, (Date)paramSetValue);
			break ;
		case T_SET_TIME:
			ps.setTime(idx, (Time)paramSetValue);
			break ;
		case T_SET_TIMESTAMP:
			ps.setTimestamp(idx, (Timestamp)paramSetValue);
			break ;
		case T_SET_OBJ:
			if(paramSetValue!=null && paramSetValue instanceof java.util.Date
					&& !(paramSetValue instanceof java.sql.Timestamp))
			{
				paramSetValue = new java.sql.Timestamp(((java.util.Date) paramSetValue)
						.getTime());
			}
			
			if(sqlType!=Integer.MIN_VALUE)
				ps.setObject(idx, paramSetValue, sqlType);
			else
				ps.setObject(idx, paramSetValue);
			break ;
		case T_SET_URL:
			ps.setURL(idx, new URL((String)paramSetValue));
			break ;
		case T_SET_NSTR:
			ps.setNString(idx, (String)paramSetValue);
			break ;
		}
	}
}
