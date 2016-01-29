package com.dw.system;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

public class DomainItem implements IXmlDataable,Comparable<DomainItem>
{
	int id = -1 ;
	
	String domain = null ;
	
	/**
	 * 最大用户数 -1表示不限
	 */
	int maxUserNum = -1 ;
	
	public DomainItem()
	{}
	
	public DomainItem(int id,String domain,int max_user)
	{
		this.id = id ;
		this.domain = domain.toLowerCase() ;
		this.maxUserNum = max_user ;
	}
	
	public int getId()
	{
		return id ;
	}
	
	
	public String getDomain()
	{
		return domain ;
	}
	
	/**
	 * 最大用户数-子域名的用户数限制
	 * @return
	 */
	public int getMaxUserNum()
	{
		return maxUserNum ;
	}
	
	public void setMaxUserNum(int mun)
	{
		maxUserNum = mun ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		xd.setParamValue("id", id) ;
		xd.setParamValue("domain", domain) ;
		if(maxUserNum>0)
			xd.setParamValue("max_user_num", maxUserNum) ;
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		id = xd.getParamValueInt32("id", -1) ;
		domain = xd.getParamValueStr("domain") ;
		maxUserNum = xd.getParamValueInt32("max_user_num", -1);
	}

	public int compareTo(DomainItem o)
	{
		return id-o.id;
	}
}
