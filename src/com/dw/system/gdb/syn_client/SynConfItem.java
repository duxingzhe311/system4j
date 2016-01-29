package com.dw.system.gdb.syn_client;

import com.dw.system.xmldata.*;

/**
 * 同步配置项
 * 
 * 它指向一个数据库表名称和相关的列定义
 * 
 * 
 * 
 * @author Jason Zhu
 *
 */
public class SynConfItem implements IXmlDataable
{
	/**
	 * 同步的表名称，也有可能是虚拟表
	 * 它在整个系统中唯一
	 */
	String tableName = null ;
	
	/**
	 * 数据表对应的结构
	 */
	XmlDataStruct tableXmlST = null ;
	
	/**
	 * 查询语句-用来获取服务器数据的访问语句
	 * 如果是整个表数据，可能不需要这个
	 */
	String selectSql = null ;
	
	/**
	 * 主键名词-用来区分每条数据记录的唯一列
	 */
	String pkColName = null ;
	
	
	public SynConfItem()
	{}
	
	public String getTableName()
	{
		return tableName ;
	}
	
	public String getPkColName()
	{
		return pkColName ;
	}
	
	public String getSelectSql()
	{
		return selectSql ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		
		xd.setParamValue("table_name", tableName) ;
		xd.setSubDataSingle("struct", tableXmlST.toXmlData()) ;
		if(pkColName!=null)
			xd.setParamValue("pk_col_name", pkColName) ;
		if(selectSql!=null)
			xd.setParamValue("select_sql", selectSql);
		
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		tableName = xd.getParamValueStr("table_name") ;
		XmlData stxd = xd.getSubDataSingle("struct") ;
		if(stxd!=null)
		{
			XmlDataStruct xds = new XmlDataStruct() ;
			xds.fromXmlData(stxd) ;
			tableXmlST = xds ;
		}
		pkColName = xd.getParamValueStr("pk_col_name") ;
		selectSql = xd.getParamValueStr("select_sql") ;
	}
}
