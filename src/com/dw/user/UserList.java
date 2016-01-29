package com.dw.user;

import java.util.*;

import com.dw.system.xmldata.xrmi.*;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

@XRmi(reg_name="user_list")
public class UserList implements IXmlDataable
{
	List<User> userList = null ;
	int pageIdx = -1 ;
	int pageSize = -1 ;
	int totalCount = -1 ;
	
	public UserList()
	{}
	
	public UserList(List<User> us,int pageidx,int pagesize,int totalc)
	{
		userList = us ;
		pageIdx = pageidx;
		pageSize = pagesize;
		totalCount = totalc ;
	}
	
	public List<User> getListUser()
	{
		return userList ;
	}
	
	public int getPageIdx()
	{
		return pageIdx ;
	}
	
	public int getPageSize()
	{
		return pageSize ;
	}
	
	public int getTotalCount()
	{
		return totalCount ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("page_idx", pageIdx);
		xd.setParamValue("page_size", pageSize);
		xd.setParamValue("total_count", totalCount);
		if(userList!=null)
		{
			List<XmlData> xds = xd.getOrCreateSubDataArray("user_list");
			for(User u:userList)
			{
				xds.add(u.toXmlData());
			}
		}
		
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		pageIdx = xd.getParamValueInt32("page_idx", -1);
		pageSize = xd.getParamValueInt32("page_size", -1);
		totalCount = xd.getParamValueInt32("total_count", -1);
		
		List<XmlData> xds = xd.getSubDataArray("user_list");
		if(xds!=null)
		{
			userList = new ArrayList<User>(xds.size());
			for(XmlData tmpxd:xds)
			{
				User u = new User();
				u.fromXmlData(tmpxd);
				userList.add(u);
			}
		}
	}
}
