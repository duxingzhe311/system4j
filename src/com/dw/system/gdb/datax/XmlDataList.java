package com.dw.system.gdb.datax;

import java.util.ArrayList;
import java.util.List;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;

public class XmlDataList
{
	private int totalCount = -1 ;
	private int pageIdx = -1 ;
	private int pageSize = -1 ;
	
	private List<XmlData> items = null ;
	
	public XmlDataList()
	{
		
	}
	
	public XmlDataList(List<XmlData> dxis,int pageidx,int pagesize,int total)
	{
		items = dxis ;
		pageIdx = pageidx ;
		pageSize = pagesize ;
		totalCount = total ;
	}
	
	public int getTotalCount()
	{
		return totalCount ;
	}
	
	public int getPageIdx()
	{
		return pageIdx ;
	}
	
	public int getPageSize()
	{
		return pageSize ;
	}
	
	public List<XmlData> getXmlDatas()
	{
		return items;
	}
	
	/**
	 * 根据内部的数据结构,获取数据列表的结构
	 * @param datastruct
	 * @return
	 */
	public static XmlDataStruct calItemListStruct(XmlDataStruct datastruct)
	{
		XmlDataStruct xds = new XmlDataStruct();
		xds.setXmlDataMember("total", XmlVal.VAL_TYPE_INT32);
		xds.setXmlDataMember("pageidx", XmlVal.VAL_TYPE_INT32);
		xds.setXmlDataMember("pagesize", XmlVal.VAL_TYPE_INT32);
		xds.setSubStruct("items",datastruct, true, true, false);
		return xds ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("total", totalCount);
		xd.setParamValue("pageidx", pageIdx);
		xd.setParamValue("pagesize", pageSize);
		
		if(items!=null&&items.size()>0)
		{
			List<XmlData> xds = xd.getOrCreateSubDataArray("items");
			xds.addAll(items);
		}
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		totalCount = xd.getParamValueInt32("total", -1);
		pageIdx = xd.getParamValueInt32("pageidx", -1);
		pageSize = xd.getParamValueInt32("pagesize", -1);
		
		items = xd.getSubDataArray("items");
	}
}
