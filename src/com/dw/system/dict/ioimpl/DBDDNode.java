package com.dw.system.dict.ioimpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dw.system.dict.DataNode;
import com.dw.system.gdb.xorm.*;
import com.dw.system.xmldata.XmlData;

@XORMClass(table_name="dd_node")
public class DBDDNode
{
	@XORMProperty(name="AutoId",has_col=true,is_pk=true,is_auto=true)
	long autoId = -1;
	
	@XORMProperty(name="NodeId",has_col=true,has_idx=true,nullable=false,order_num=5)
	int nodeId = -1 ;
	
	@XORMProperty(name="ParentNodeId",has_col=true,nullable=false,order_num=6)
	int parentNodeId = -1 ;
	
	/**
	 * 所属模块名称--比如一些字典是某个模块提供
	 */
	@XORMProperty(name="ModuleName",has_col=true,max_len=50,has_idx=true,order_num=7)
	String moduleName = null ;
	
	@XORMProperty(name="ClassId",has_col=true,has_idx=true,nullable=false,order_num=10)
	int classId = -1 ;
	
	@XORMProperty(name="Name",has_col=true,has_idx=true,max_len=100,nullable=false,order_num=20)
	String nodeName = null ;
	
	@XORMProperty(name="NameCn",has_col=true,max_len=100,order_num=30)
	String nameCn = null ;
	
	@XORMProperty(name="NameEn",has_col=true,max_len=100,order_num=40)
	String nameEn = null ;
	
	@XORMProperty(name="OrderNo",has_col=true,order_num=50)
	int orderNo = -1;
	
	@XORMProperty(name="IsVisiable",has_col=true,order_num=53)
	boolean bVisiable = true;
	
	/**
	 * 是否是缺省值
	 */
	@XORMProperty(name="IsDefault",has_col=true,default_str_val="0",order_num=54)
	boolean bDefault = false;

	@XORMProperty(name="IsForbidden",has_col=true,order_num=55)
	boolean bForbidden = false;
	
	@XORMProperty(name="CreationDate",has_col=true,order_num=60)
	Date createDate = null ;
	
	@XORMProperty(name="LastUpdateDate",has_col=true,order_num=70)
	Date lastUpdateDate = null ;
	
	HashMap<String,String> extendsInfo = null ;
	
	@XORMProperty(name="ExtendInfo",has_col=true,read_on_demand=false,order_num=80)
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
	
	public DBDDNode()
	{}
	
	public DBDDNode(int classid,DataNode dn)
	{
		this.autoId = dn.getAutoId() ;
		
		this.nodeId = dn.getId() ;
		
		this.classId = classid ;
		
		this.parentNodeId = dn.getParentNodeId() ;
		
		this.nodeName = dn.getName() ;
		
		this.nameCn = dn.getNameCN() ;
		
		this.nameEn = dn.getNameEn() ;
		
		this.orderNo = dn.getOrderNo();
		
		this.bVisiable = dn.isVisiable();
		
		this.bDefault = dn.isDefaultNode() ;

		this.bForbidden = dn.isForbidden();
		
		createDate = dn.getCreateTime();
		lastUpdateDate = dn.getLastUpdateTime() ;
		
		Set<String> ns = dn.getExtendedAttrNames();
		if(ns!=null)
		{
			extendsInfo = new HashMap<String,String> () ;
			for(String n:ns)
			{
				extendsInfo.put(n, dn.getExtendedAttr(n));
			}
		}
	}
}
