package com.dw.system.gdb.conf.autofit;

import java.io.*;
import java.util.*;

import com.dw.system.xmldata.*;

public class JavaColumnInfo implements IXmlDataable
{
	
	public static int XmlValType2SqlType(XmlVal.XmlValType vt)
	{
		if (vt == XmlVal.XmlValType.vt_xml_schema)
		{
			return java.sql.Types.VARCHAR;
		}
		else if (vt == XmlVal.XmlValType.vt_byte_array)
		{
			return java.sql.Types.BLOB;
		}
		else if (vt == XmlVal.XmlValType.vt_date)
		{
			//return java.sql.Types.DATE;
			return java.sql.Types.TIMESTAMP;
		}
		else if (vt == XmlVal.XmlValType.vt_double)
		{
			return java.sql.Types.DOUBLE;
		}
		else if (vt == XmlVal.XmlValType.vt_float)
		{
			return java.sql.Types.FLOAT;
		}
		else if (vt == XmlVal.XmlValType.vt_int64)
		{
			return java.sql.Types.BIGINT;
		}
		else if (vt == XmlVal.XmlValType.vt_int32)
		{
			return java.sql.Types.INTEGER;
		}
		else if (vt == XmlVal.XmlValType.vt_int16)
		{
			return java.sql.Types.SMALLINT;
		}
		else if (vt == XmlVal.XmlValType.vt_byte)
		{
			return java.sql.Types.TINYINT;
		}
		else if (vt == XmlVal.XmlValType.vt_string)
		{
			return java.sql.Types.VARCHAR;
		}
		else if (vt == XmlVal.XmlValType.vt_bool)
		{
			return java.sql.Types.BIT;
		}
		else if (vt == XmlVal.XmlValType.vt_xml_data)
		{
			return java.sql.Types.CLOB;
		}
		else
		{
			throw new IllegalArgumentException("unknow xml val type="+vt);
		}
	}
	
	/**
	 * 根据对象类型，获得对应的jdbc sql类型
	 * @param c
	 * @return
	 */
	public static int Class2SqlType(Class c)
	{
		XmlVal.XmlValType xvt = XmlVal.class2VT(c);
		if(xvt==null)
		{
			xvt = XmlVal.XmlValType.vt_string ;
		}
		return XmlValType2SqlType(xvt) ;
	}

	private String columnName = null;
	
	private boolean bPk = false;

	private XmlVal.XmlValType valType = XmlVal.XmlValType.vt_string;
	
	transient private int sqlValType =  java.sql.Types.VARCHAR;

	private int maxLen = -1;

	private boolean bUnique = false;

	private boolean bHasIdx = true;

	private boolean bAutoVal = false;
	
	/**
	 * 自动值得起始值
	 */
	private long autoValStart = -1 ;
	
	private String defaultStrVal = null ;
	
	private boolean bReadOnDemand = false;
	
	private boolean bUpdateAsSingle = false;

	// private boolean bPrimaryKey = false;

	public JavaColumnInfo()
	{
	}

	public JavaColumnInfo(String coln,boolean b_pk, XmlVal.XmlValType vt, int maxlen,
			boolean hasidx, boolean unique, boolean autoval,long autoval_st)
	{
		this(coln,b_pk, vt, maxlen,hasidx, unique, autoval,autoval_st,null,false,false);
	}
	
	public JavaColumnInfo(String coln,boolean b_pk, XmlVal.XmlValType vt, int maxlen,
			boolean hasidx, boolean unique, boolean autoval,long autoval_st,String default_strv,
			boolean b_read_ondemand,boolean b_update_as_single)
	{
		columnName = coln;
		bPk = b_pk ;
		valType = vt;
		
		sqlValType = XmlValType2SqlType(vt) ;
		
		maxLen = maxlen;
		bHasIdx = hasidx;
		bUnique = unique;
		bAutoVal = autoval;
		this.autoValStart = autoval_st ;
		defaultStrVal = default_strv ;
		// bPrimaryKey = pk ;
		bReadOnDemand = b_read_ondemand ;
		bUpdateAsSingle = b_update_as_single ;
	}

	/**
	 * 得到列名称
	 * 
	 * @return
	 */
	public String getColumnName()
	{
		return columnName;
	}
	
	public boolean isPk()
	{
		return bPk ;
	}

	public XmlVal.XmlValType getValType()
	{
		return valType;
	}
	
	/**
	 * 判断是否是字符串类型的自增值主键
	 * @return
	 */
	public boolean isAutoStringValuePk()
	{
		if(!bPk)
			return false;
		
		if(valType!=XmlVal.XmlValType.vt_string)
			return false;
		
		return bAutoVal;
	}
	
	public String getDefaultValStr()
	{
		return defaultStrVal;
	}
	/**
	 * java.util.Types中指定的Sql类型
	 * 
	 * @return
	 */
	public int getSqlValType()
	{
		return sqlValType ;
	}

	public int getMaxLen()
	{
		return maxLen;
	}

	public boolean hasIdx()
	{
		return bHasIdx;
	}

	// public boolean isPrimaryKey()
	// {
	// return bPrimaryKey ;
	// }

	public boolean isUnique()
	{
		return bUnique;
	}

	public boolean isAutoVal()
	{
		return bAutoVal;
	}
	
	public long getAutoValStart()
	{
		return this.autoValStart;
	}
	/**
	 * 存在数据库列,但一般情况下读取的时候,根据需要
	 * 例如,列表时有可能不读取内容
	 * @return
	 */
	public boolean isReadOnDemand()
	{
		return bReadOnDemand ;
	}
	
	/**
	 * 本列是否只能独立更新,如blob对应的列应该使用true
	 * @return
	 */
	public boolean isUpdateAsSingle()
	{
		return bUpdateAsSingle ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("col_name", columnName);
		xd.setParamValue("val_type", XmlVal.ValType2StrType(valType));
		xd.setParamValue("max_len", maxLen);
		xd.setParamValue("is_unique", bUnique);
		xd.setParamValue("has_idx", bHasIdx);
		xd.setParamValue("is_autoval", bAutoVal);

		// xd.setParamValue("is_pk", bPrimaryKey);
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		columnName = xd.getParamValueStr("col_name");
		String tmps = xd.getParamValueStr("val_type");
		if (tmps != null)
		{
			valType = XmlVal.StrType2ValType(tmps);
			sqlValType = XmlValType2SqlType(valType) ;
		}

		maxLen = xd.getParamValueInt32("max_len", -1);
		bUnique = xd.getParamValueBool("is_unique", false);
		bHasIdx = xd.getParamValueBool("has_idx", true);
		bAutoVal = xd.getParamValueBool("is_autoval", false);
		// bPrimaryKey = xd.getParamValueBool("is_pk", false);
	}
}
