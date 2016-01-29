package com.dw.system.gdb.datax;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.dw.system.gdb.datax.DataXIndex.IdxType;
import com.dw.system.gdb.datax.DataXIndex.JavaColumnAStructItem;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlDataMember;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * Single Value Multi Col单值多列索引
 * 
 * 单值意味着主键采用XmlData自带的id，并且列中的值和XmlData一一对应
 * @author Jason Zhu
 */
@XRmi(reg_name="datax_index_svmc")
public class DataXIndexSVMC extends DataXIndex
{
	private List<XmlDataPath> idxStructPaths = null ;//new ArrayList<String>();
	
//	DataXIndexSVMC(DataXBase dxb,DataXClass xdc)
//	{
//		super(dxb,xdc);
//	}
	
	public DataXIndexSVMC()
	{}
	
	
	public XmlData toXmlData()
	{
		XmlData xd = super.toXmlData() ;
		
		String[] tmps = new String[idxStructPaths.size()];
		for(int i = 0 ; i < tmps.length ; i ++)
		{
			tmps[i] = idxStructPaths.get(i).toString() ;
		}
		xd.setParamValues("xps", tmps);
		
		return xd ;
	}
	
	public void fromXmlData(XmlData xd)
	{
		super.fromXmlData(xd);
		
		String[] tmps = xd.getParamValuesStr("xps");

		idxStructPaths = new ArrayList<XmlDataPath>();
		if(tmps!=null)
		{
			for(String s:tmps)
			{
				idxStructPaths.add(new XmlDataPath(s));
			}
		}
	}
	
	public DataXIndexSVMC(
			int id,String name,
			List<String> idxsps)
	{
		super(id,name);
		
		idxStructPaths = new ArrayList<XmlDataPath>() ;
		for(String ps:idxsps)
		{
			idxStructPaths.add(new XmlDataPath(ps));
		}
	}
	
	public void update(String name,List<String> idxsps)
	{
		this.setName(name);
		idxStructPaths = new ArrayList<XmlDataPath>() ;
		for(String ps:idxsps)
		{
			idxStructPaths.add(new XmlDataPath(ps));
		}
	}
	
	public String toString()
	{
		return name +":SVMC";
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof DataXIndexSVMC))
			return false;
		
		if(!super.equals(o))
			return false;
		
		DataXIndexSVMC mdix = (DataXIndexSVMC)o;
		
		int c = idxStructPaths.size();
		if(c!=mdix.idxStructPaths.size())
			return false;
		
		HashSet<XmlDataPath> hs = new HashSet<XmlDataPath>();
		for(int i = 0 ; i < c ; i ++)
			hs.add(idxStructPaths.get(i));
		
		for(int i = 0 ; i < c ; i ++)
		{
			if(!hs.contains(mdix.idxStructPaths.get(i)))
				return false;
		}
		return true ;
	}
	
	public List<XmlDataPath> getIdxStructPaths()
	{
		return idxStructPaths;
	}
	
	@Override
	public boolean isMultiValue()
	{
		return false;
	}
	
	public boolean checkValid(XmlDataStruct xds,StringBuffer failedreson)
	{
		if(idxStructPaths==null||idxStructPaths.size()<=0)
		{
			failedreson.append("xml data pathes is null!");
			return false;
		}
		
		
		for(XmlDataPath dxp:idxStructPaths)
		{
			XmlDataMember si = xds.getXmlDataMemberByPath(dxp.getPath());
			if(si==null)
			{
				failedreson.append("path data member :"+dxp.toString()+" is not find");
				return false;
			}
			
			if(si.isArray())
			{
				failedreson.append("path data member :"+dxp.toString()+" is array!");
				return false;
			}
		}
		
		
		return true ;
	}
	
	@Override
	public IdxType getIdxType()
	{
		return IdxType.SINGLE_VAL_MULTI_COL;
	}

	public void setIndexPath(String path)
	{
		XmlDataPath dxp = new XmlDataPath(path) ;
		if(!idxStructPaths.contains(dxp))
		{
			idxStructPaths.add(dxp) ;
			
			javaTableInfo = null ;
			columnInfos = null ;
		}
	}
	
	public void unsetIndexPath(String path)
	{
		XmlDataPath dxp = new XmlDataPath(path) ;
		if(idxStructPaths.remove(dxp))
		{
			javaTableInfo = null ;
			columnInfos = null ;
		}
	}
	
	
	public List<JavaColumnAStructItem> initColumnInfos()
	{
		List<JavaColumnAStructItem> tmpjcis = new ArrayList<JavaColumnAStructItem>();
		for(XmlDataPath s:idxStructPaths)
		{
			XmlDataMember si = dataxClass.getDataStruct().getXmlDataMemberByPath(s.getPath()) ;
			if(si==null)
				continue ;
			
			JavaColumnAStructItem jc_si = new JavaColumnAStructItem(s,si);
			tmpjcis.add(jc_si);
		}
		
		return tmpjcis ;
	}
	
	@Override
	public void OnSetXmlData(Connection conn,long xdid, XmlData xd)
		throws Exception
	{
		//del first
		delIdxIO(conn,xdid);
		
		//insert
		insertXmlDataIdxIO(conn,xdid,xd) ;
	}
	
	

	private boolean insertXmlDataIdxIO(Connection conn,long dxid,XmlData xd) throws Exception
	{
		PreparedStatement ps = null;
		// boolean oldcommit = conn.getAutoCommit();
		try
		{
			String sqlstr = SQL_Insert() ;
			System.out.println("[DataXIndexSVMC insertXmlDataIdxIO]=="+sqlstr);
			ps = conn.prepareStatement(sqlstr);
			
			long tt = System.currentTimeMillis();
			ps.setObject(1, dxid);
			
			List<JavaColumnAStructItem> cols = this.getColumnInfos();
			int c = cols.size();
			for(int i = 0 ; i < c ; i ++)
			{
				JavaColumnAStructItem jcsi = cols.get(i);
				Object ov = extractSingleValFromXmlData(xd,jcsi.getMemberPath().getPathNames()) ;
				//ps.setObject(i+2, ov);
				
				if(ov==null)
					ps.setNull(i+2, jcsi.columnInfo.getSqlValType());
				else
					ps.setObject(i+2, ov);
			}

			return ps.execute();
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
		}
	}
	
	@Override
	public long[] findByCondition(String cond_str, int pageidx, int pagesize)
	{
		return null;
	}

	

}
