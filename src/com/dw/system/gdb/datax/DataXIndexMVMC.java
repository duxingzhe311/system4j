package com.dw.system.gdb.datax;

import java.sql.*;
import java.util.*;

import com.dw.system.gdb.datax.DataXIndex.IdxType;
import com.dw.system.gdb.datax.DataXIndex.JavaColumnAStructItem;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * Multi Value Multi Col多值多列索引
 * 
 * 主要针对XmlData中的多值子XmlData进行——子xml提取的内容必须单值
 * 单值意味着主键采用XmlData自带的id，并且列中的值和XmlData一一对应
 * @author Jason Zhu
 */
@XRmi(reg_name="datax_index_mvmc")
public class DataXIndexMVMC extends DataXIndex
{
	/**
	 * 对应多值的指向多值子XmlData路径
	 */
	private XmlDataPath multiSubXmlDataPath = null ;
	/**
	 * 在每个子XmlData内的单值列
	 */
	private String[] subXmlDataCols = null ;
	
//	DataXIndexMVMC(DataXBase dxb,DataXClass xdc)
//	{
//		super(dxb,xdc);
//	}
	
	public DataXIndexMVMC()
	{}
	
	public String toString()
	{
		return name +":MVMC";
	}
	
	public XmlData toXmlData()
	{
		XmlData xd = super.toXmlData() ;
		
		xd.setParamValue("sub_xp", multiSubXmlDataPath.toString());
		xd.setParamValues("sub_cols", subXmlDataCols);
		return xd ;
	}
	
	public void fromXmlData(XmlData xd)
	{
		super.fromXmlData(xd);
		
		multiSubXmlDataPath = new XmlDataPath(xd.getParamValueStr("sub_xp"));
		subXmlDataCols = xd.getParamValuesStr("sub_cols");
	}
	
	public DataXIndexMVMC(
			int id,String name,
			String subxp,String[] sub_cols)
	{
		super(id,name);
		
		multiSubXmlDataPath = new XmlDataPath(subxp) ;
		subXmlDataCols = sub_cols ;
	}
	
	public void update(String name,String subxp,String[] sub_cols)
	{
		this.setName(name);
		multiSubXmlDataPath = new XmlDataPath(subxp) ;
		subXmlDataCols = sub_cols ;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof DataXIndexMVMC))
			return false;
		
		if(!super.equals(o))
			return false;
		
		DataXIndexMVMC mdix = (DataXIndexMVMC)o;
		if(!multiSubXmlDataPath.equals(mdix.multiSubXmlDataPath))
			return false ;
		
		
		if(subXmlDataCols.length!=mdix.subXmlDataCols.length)
			return false;
		
		HashSet<String> hs = new HashSet<String>();
		for(int i = 0 ; i < subXmlDataCols.length ; i ++)
			hs.add(subXmlDataCols[i]);
		for(int i = 0 ; i < subXmlDataCols.length ; i ++)
		{
			if(!hs.contains(mdix.subXmlDataCols[i]))
				return false;
		}
		return true ;
	}
	
	public XmlDataPath getMultiSubXmlDataPath()
	{
		return multiSubXmlDataPath;
	}
	
	public String[] getSubXmlDataCols()
	{
		return subXmlDataCols ;
	}
	
	void setIdxStructPath(String multisub_xmldatapath,String[] sub_colname)
	{
		if(multisub_xmldatapath==null||multisub_xmldatapath.equals(""))
			throw new IllegalArgumentException("invalid xml data path");
		
		if(sub_colname==null||sub_colname.length<=0)
			throw new IllegalArgumentException("invalid sub column names");
		
		
		multiSubXmlDataPath = new XmlDataPath(multisub_xmldatapath);
		if(!multiSubXmlDataPath.isStruct())
			throw new IllegalArgumentException("not sub xml path:"+multisub_xmldatapath);
		subXmlDataCols = sub_colname ;
	}
	
	//private transient String[] multiSubDataArrayPath = null ;
	
	@Override
	protected List<JavaColumnAStructItem> initColumnInfos()
	{
		if(multiSubXmlDataPath==null)
			throw new RuntimeException("multi sub xml path is null or empty!");
		
//		StringTokenizer st = new StringTokenizer(multiSubXmlDataPath,"/");
//		int c = st.countTokens() ;
//		multiSubDataArrayPath = new String[c];
//		for(int i = 0 ; i < c ; i ++)
//			multiSubDataArrayPath[i] = st.nextToken() ;
		
		if(subXmlDataCols==null||subXmlDataCols.length<=0)
			throw new RuntimeException("sub xmldata cols is not be set!");
		
		XmlDataStruct subds = dataxClass.getDataStruct().getSubStructByPath(multiSubXmlDataPath.getPath());
		if(subds==null)
			throw new RuntimeException("cannot find sub xml struct by path="+multiSubXmlDataPath);
		
		if(!subds.isArray())
			throw new RuntimeException("sub xml struct with path="+multiSubXmlDataPath+" is not array!");
		
		ArrayList<JavaColumnAStructItem> rets = new ArrayList<JavaColumnAStructItem>();
		
		for(String s:subXmlDataCols)
		{
			if(s==null||s.equals(""))
				continue ;
			
			XmlDataMember si = subds.getXmlDataMember(s);
			if(si==null)
				continue ;
			
			JavaColumnAStructItem jc_si = new JavaColumnAStructItem(multiSubXmlDataPath.getSubPath(s,false),si);
			rets.add(jc_si);
		}
		
		return rets;
	}
	
	public boolean checkValid(XmlDataStruct xds,StringBuffer failedreson)
	{
		if(multiSubXmlDataPath==null)
		{
			failedreson.append("sub xml data path is null!");
			return false;
		}
		
		if(subXmlDataCols==null||subXmlDataCols.length<=0)
		{
			failedreson.append("sub data columns is null!");
			return false;
		}
		
		XmlDataStruct tmpxds = xds.getSubStructByPath(multiSubXmlDataPath.getPath()) ;
		if(tmpxds==null)
		{
			failedreson.append("cannot get xmldata struct with sub xml data path!");
			return false;
		}
		
		if(!tmpxds.isArray())
		{
			failedreson.append("sub xml data is not array!");
			return false;
		}
		
		for(String coln:subXmlDataCols)
		{
			XmlDataMember si = tmpxds.getXmlDataMember(coln);
			if(si==null)
			{
				failedreson.append("sub xml data member :"+coln+" is not find");
				return false;
			}
			
			if(si.isArray())
			{
				failedreson.append("sub xml data member :"+coln+" is array!");
				return false;
			}
		}
		
		
		return true ;
	}
	
	@Override
	public boolean isMultiValue()
	{
		return true;
	}

	@Override
	public void OnSetXmlData(Connection conn,long xdid, XmlData xd)
		throws Exception
	{
//		del first
		delIdxIO(conn,xdid);
		
		//insert
		insertXmlDataIdxIO(conn,xdid,xd) ;
	}


	@Override
	public long[] findByCondition(String cond_str, int pageidx, int pagesize)
	{
		
		return null;
	}

	
	
	@Override
	public IdxType getIdxType()
	{
		return IdxType.MULTI_VAL_MULTI_COL;
	}

	private boolean insertXmlDataIdxIO(Connection conn,long dxid,XmlData xd) throws Exception
	{
		List<JavaColumnAStructItem> cols = this.getColumnInfos();
		if(cols==null||cols.size()<=0)
			throw new Exception("cannot get column info!");
		
		if(multiSubXmlDataPath==null)
			throw new Exception("no sub data array path,may be it is not be inited");
		
		int c = cols.size();
		
		PreparedStatement ps = null;

		List<XmlData> subxds = extractSubArrayXmlDataByPath(xd,multiSubXmlDataPath.getPathNames());
		if(subxds==null||subxds.size()<=0)
			return true ;
		
		
		try
		{
			String sqlstr = SQL_Insert() ;
			System.out.println("[DataXIndexMVMC insertXmlDataIdxIO]="+sqlstr);
			ps = conn.prepareStatement(sqlstr);
			ps.setObject(1, dxid);
			for(XmlData tmpxd : subxds)
			{
				for(int i = 0 ; i < subXmlDataCols.length ; i ++)
				{
					JavaColumnAStructItem jcsi = cols.get(i);
					Object ov = tmpxd.getParamValue(subXmlDataCols[i]) ;
					if(ov==null)
						ps.setNull(i+2, jcsi.columnInfo.getSqlValType());
					else
						ps.setObject(i+2, ov);
				}
				
				ps.execute();
			}
			
			
			
			
			
			return true ;
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
		}
	}
}
