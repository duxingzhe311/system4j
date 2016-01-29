package com.dw.system.dict.ioimpl;

import java.util.*;

import com.dw.system.Convert;
import com.dw.system.dict.DataClass;
import com.dw.system.dict.DataClassIO;
import com.dw.system.dict.DataNode;
import com.dw.system.gdb.*;

public class DBDDIO extends DataClassIO
{

	@Override
	public int[] getAllClassIds(String modulen) throws  Exception
	{
		Hashtable ht = new Hashtable() ;
		if(Convert.isNotNullEmpty(modulen))
			ht.put("@ModuleName", modulen) ;
		DBResult dbr = GDB.getInstance().accessDB("data_dict.GetAllClassIds", ht);
		DataTable dt = dbr.getResultFirstTable() ;
		if(dt==null)
			return null ;
		
		int rn = dt.getRowNum() ;
		int[] rets = new int[rn];
		for(int i = 0 ; i < rn ; i ++)
		{
			Number n = (Number)dt.getRow(i).getValue(0) ;
			if(n!=null)
			{
				rets[i] = n.intValue() ;
			}
		}
		return rets ;
	}

	@Override
	public DataClass loadDataClass(String modulen,int classid)
	{
		
		return null;
	}

	@Override
	public List<DataNode> loadAllDataNodes(String modulen,int classid) throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@ClassId",classid);
		if(Convert.isNotNullEmpty(modulen))
			ht.put("@ModuleName", modulen) ;
		
		List<DBDDNode> ns = (List<DBDDNode>)GDB.getInstance().accessDBPageAsXORMObjList("data_dict.GetAllNodesByClassId", ht, DBDDNode.class, 0, -1) ;
		ArrayList<DataNode> rets = new ArrayList<DataNode>() ;
		if(ns!=null)
		{
			for(DBDDNode n:ns)
			{
				DataNode dn = new DataNode(n.autoId,n.nodeId,n.parentNodeId,n.nodeName,
						n.nameCn,n.nameEn,
						n.orderNo,n.bVisiable,n.bForbidden,
						n.createDate,n.lastUpdateDate,
						n.extendsInfo,n.bDefault) ;
				
				rets.add(dn);
			}
		}
		return rets;
	}

	public void addDataNode(String modulen,int classid,DataNode dn) throws Exception
	{
		DBDDNode ddn = new DBDDNode(classid,dn) ;
		ddn.moduleName = modulen ;
		GDB.getInstance().addXORMObjWithNewId(ddn);
	}
	
	public void updateDataNode(String modulen,long autoid,DataNode dn) throws Exception
	{
		DBDDNode ddn = new DBDDNode(dn.getBelongToClassId(),dn) ;
		ddn.moduleName = modulen ;
		GDB.getInstance().updateXORMObjToDB(autoid, ddn);
	}
	
	public void delDataNode(long autoid) throws Exception
	{
		GDB.getInstance().deleteXORMObjFromDB(autoid, DBDDNode.class);
	}
	
	public void setDefaultDataNode(String modulen,int classid,int dnid)
		throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@ClassId",classid);
		ht.put("@NodeId",dnid);
		if(Convert.isNotNullEmpty(modulen))
			ht.put("@ModuleName", modulen) ;
		
		GDB.getInstance().accessDB("data_dict.SetDefaultNode", ht);
	}
}
