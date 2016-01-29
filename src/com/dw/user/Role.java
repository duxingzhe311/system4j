package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.xmldata.xrmi.*;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

@XRmi(reg_name="security_role")
public class Role implements IXmlDataable
{
	String id = null;

	String name = null;
	
	String title = null ;

	String desc = null;

	// / <summary>
	// / 角色扩展信息
	// / </summary>
	String extInfo = null;

	// /// <summary>
	// /// 所属的组织机构节点
	// /// 0-表示没有
	// /// </summary>
	// internal int belongToOrgNodeId = 0;

	public Role(String id, String name, String desc)
	{
		this.id = id;
		this.name = name;
		this.desc = desc;
	}
	
	public Role(String id, String name,String title, String desc)
	{
		this.id = id;
		this.name = name;
		this.title = title ;
		this.desc = desc;
	}

	public Role()
	{
	}
	
	//support db map
	private String get_Id()
	{
		return id ;
	}
	
	private void set_Id(String i)
	{
		id = i ;
	}
	
	private String get_Name()
	{
		return name ;
	}
	
	private void set_Name(String n)
	{
		name = n ;
	}
	
	private String get_Title()
	{
		return title ;
	}
	
	private void set_Title(String t)
	{
		title = t ;
	}
	
	private String get_Desc()
	{
		return desc ;
	}
	
	private void set_Desc(String d)
	{
		desc = d ;
	}
	
	private String get_ExtInfo()
	{
		return extInfo ;
	}
	
	private void set_ExtInfo(String ei)
	{
		extInfo = ei ;
	}

	
	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
	
	public String getTitle()
	{
		if(Convert.isNotNullEmpty(title))
			return title ;
		return name ;
	}

	public String getDesc()
	{
		return desc;
	}
	
	public String toString()
	{
		return name ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("id", id);
		
		xd.setParamValue("name", name);
		if(desc!=null)
			xd.setParamValue("desc",desc);

		if(extInfo!=null)
			xd.setParamValue("ext_info", extInfo);
		
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		id = xd.getParamValueStr("id");
		name = xd.getParamValueStr("name");
		desc = xd.getParamValueStr("desc");
		extInfo = xd.getParamValueStr("ext_info");
	}
}
