package com.dw.system.gdb.syn_client;

import com.dw.system.xmldata.*;

/**
 * ͬ��������
 * 
 * ��ָ��һ�����ݿ�����ƺ���ص��ж���
 * 
 * 
 * 
 * @author Jason Zhu
 *
 */
public class SynConfItem implements IXmlDataable
{
	/**
	 * ͬ���ı����ƣ�Ҳ�п����������
	 * ��������ϵͳ��Ψһ
	 */
	String tableName = null ;
	
	/**
	 * ���ݱ��Ӧ�Ľṹ
	 */
	XmlDataStruct tableXmlST = null ;
	
	/**
	 * ��ѯ���-������ȡ���������ݵķ������
	 * ��������������ݣ����ܲ���Ҫ���
	 */
	String selectSql = null ;
	
	/**
	 * ��������-��������ÿ�����ݼ�¼��Ψһ��
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
