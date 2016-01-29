package com.dw.system.gdb;

import com.dw.system.Convert;
import com.dw.system.xmldata.*;

public class DataColumn implements IXmlDataable
{
	private String name = null ;
	private String title = null ;
	private Class dataType = null ;
	/**
	 * jdbc¿‡–Õ
	 */
	int jdbcType = -1 ;
	
	DataColumn()
	{}
	
	public DataColumn(String name,Class datatype)
	{
		//this.srcName = name ;
		this.name = name.toUpperCase() ;
		this.dataType = datatype ;
	}
	
	public DataColumn(String name,Class datatype,int jdbctype)
	{
		//this.srcName = name ;
		this.name = name.toUpperCase() ;
		this.dataType = datatype ;
		this.jdbcType = jdbctype ;
	}
	
	public DataColumn(String name,String title,Class datatype)
	{
		//this.srcName = name ;
		this.name = name.toUpperCase() ;
		this.title = title ;
		this.dataType = datatype ;
	}
	
	public String getName()
	{
		return name ;
	}
	
	public String getTitle()
	{
		if(Convert.isNotNullEmpty(title))
			return title ;
		return name ;
	}
	
	public Class getDataType()
	{
		return dataType ;
	}
	
	public int getJdbcType()
	{
		return jdbcType ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		xd.setParamValue("name", name) ;
		if(title!=null)
			xd.setParamValue("title", title) ;
		if(dataType!=null)
			xd.setParamValue("type", dataType.getCanonicalName()) ;
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		name = xd.getParamValueStr("name") ;
		title = xd.getParamValueStr("title");
		String strdt = xd.getParamValueStr("type") ;
		if(Convert.isNotNullEmpty(strdt))
		{
			try
			{
				dataType = Class.forName(strdt) ;
			}
			catch(Exception e)
			{}
		}
	}
}
