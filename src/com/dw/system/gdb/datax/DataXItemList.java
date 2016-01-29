package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * 数据项列表
 * @author Jason Zhu
 */
@XRmi(reg_name = "datax_itemlist")
public class DataXItemList implements IXmlDataable
{
	private int totalCount = -1 ;
	private int pageIdx = -1 ;
	private int pageSize = -1 ;
	
	private List<DataXItem> items = null ;
	
	public DataXItemList()
	{
		
	}
	
	public DataXItemList(List<DataXItem> dxis,int pageidx,int pagesize,int total)
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
	
	public List<DataXItem> getDataXItems()
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
		if(datastruct!=null)
		{
			XmlDataStruct tmpxds = new XmlDataStruct();
			datastruct = datastruct.copyMe();
			tmpxds.setXmlDataMember("xd_id", XmlVal.VAL_TYPE_INT64,false,false,-1,XmlDataStruct.StoreType.Normal);
			tmpxds.setSubStruct("xmldata", datastruct,false,true,false);
			xds.setSubStruct("items",tmpxds, true, true, false);
		}
		
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
			for(DataXItem dxi:items)
			{
				xds.add(dxi.toXmlData());
			}
		}
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		totalCount = xd.getParamValueInt32("total", -1);
		pageIdx = xd.getParamValueInt32("pageidx", -1);
		pageSize = xd.getParamValueInt32("pagesize", -1);
		
		List<XmlData> xds = xd.getSubDataArray("items");
		if(xds!=null&&xds.size()>0)
		{
			items = new ArrayList<DataXItem>();
			for(XmlData tmpxd:xds)
			{
				DataXItem dxi = new DataXItem();
				dxi.fromXmlData(tmpxd);
				items.add(dxi);
			}
		}
	}
}
