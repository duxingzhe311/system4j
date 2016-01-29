package com.dw.system.gdb.datax;

import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * ������
 * @author Jason Zhu
 */
@XRmi(reg_name = "datax_item")
public class DataXItem implements IXmlDataable
{
	/**
	 * ���������������ݵĽṹ,��ȡDataXItem��Ӧ�����ݽṹ
	 * @param datastruct �ڲ����ݽṹ
	 * @return
	 */
	public static XmlDataStruct calItemStruct(XmlDataStruct datastruct)
	{
		XmlDataStruct xds = new XmlDataStruct();
		xds.setXmlDataMember("xd_id", XmlVal.VAL_TYPE_INT64);
		xds.setSubStruct("xmldata",datastruct, false, false, false) ;
		return xds ;
	}
	
	private long dataxId = -1 ;
	private XmlData xmlData = null ;
	
	public DataXItem()
	{}
	
	public DataXItem(long dxid,XmlData xd)
	{
		dataxId = dxid ;
		xmlData = xd ;
	}
	
	public long getDataXId()
	{
		return dataxId ;
	}
	
	public XmlData getXmlData()
	{
		return xmlData ;
	}	

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("xd_id", dataxId);
		xd.setSubDataSingle("xmldata", xmlData);
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		dataxId = xd.getParamValueInt64("xd_id", -1);
		xmlData = xd.getSubDataSingle("xmldata");
	}
}
