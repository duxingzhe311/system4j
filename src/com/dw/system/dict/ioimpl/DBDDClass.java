package com.dw.system.dict.ioimpl;

import java.util.*;

import com.dw.system.gdb.xorm.*;
import com.dw.system.xmldata.XmlData;

@XORMClass(table_name="dd_class")
public class DBDDClass
{
	@XORMProperty(name="AutoId",has_col=true,is_pk=true,is_auto=true)
	long autoId = -1;
	
	@XORMProperty(name="ClassId",has_col=true,has_idx=true,is_unique_idx=true,nullable=false,order_num=10)
	int classId = -1 ;
	
	@XORMProperty(name="ClassName",has_col=true,has_idx=true,is_unique_idx=true,max_len=100,nullable=false,order_num=20)
	String className = null ;
	
	@XORMProperty(name="NameCn",has_col=true,max_len=100,order_num=30)
	String nameCn = null ;
	
	@XORMProperty(name="NameEn",has_col=true,max_len=100,order_num=40)
	String nameEn = null ;
	
	@XORMProperty(name="Version",has_col=true,max_len=20,order_num=50)
	String version = null ;
	
	@XORMProperty(name="CreationDate",has_col=true,order_num=60)
	Date createDate = null ;
	
	@XORMProperty(name="LastUpdateDate",has_col=true,order_num=70)
	Date lastUpdateDate = null ;
	
	HashMap<String,String> extendsInfo = null ;
	
	@XORMProperty(name="ExtendInfo",has_col=true,read_on_demand=false,order_num=70)
	private byte[] get_ExtendInfo()
	{
		if(extendsInfo==null)
			return null ;
		
		XmlData xd = new XmlData() ;
		for(Map.Entry<String,String> n2v:extendsInfo.entrySet())
		{
			xd.setParamValue(n2v.getKey(), n2v.getValue()) ;
		}
		
		return xd.toBytesWithUTF8() ;
	}
	private void set_ExtendInfo(byte[] b) throws Exception
	{
		if(b==null||b.length<=0)
			extendsInfo = null ;
		
		XmlData xd = XmlData.parseFromByteArrayUTF8(b) ;
		String[] ns = xd.getParamNames() ;
		if(ns==null)
			return ;
		
		extendsInfo = new HashMap<String,String>() ;
		for(String n:ns)
		{
			extendsInfo.put(n, xd.getParamValueStr(n));
		}
	}
	
}
