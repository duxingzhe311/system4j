package com.dw.system.gdb.datax;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.dw.system.gdb.datax.DataXIndex.IdxType;
import com.dw.system.gdb.datax.DataXIndex.JavaColumnAStructItem;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlDataMember;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * Multi Value Single Col多值单列索引
 * 
 * 主要针对XmlData中的多值数组进行
 * 多值意味着主键采用自身的id，并且列中的值和XmlData一一对应
 * @author Jason Zhu
 */
@XRmi(reg_name="datax_index_mvsc")
public class DataXIndexMVSC extends DataXIndex
{
	private XmlDataPath idxStructPath = null;
	
//	DataXIndexMVSC(DataXBase dxb,DataXClass xdc)
//	{
//		super(dxb,xdc);
//	}
	
	public DataXIndexMVSC()
	{}
	
	public String toString()
	{
		return name +":MVSC";
	}
	
	public XmlData toXmlData()
	{
		XmlData xd = super.toXmlData() ;
		
		xd.setParamValue("xp", idxStructPath.toString());
		return xd ;
	}
	
	public void fromXmlData(XmlData xd)
	{
		super.fromXmlData(xd);
		
		idxStructPath = new XmlDataPath(xd.getParamValueStr("xp"));
	}
	
	
	public DataXIndexMVSC(
			int id,String name,
			String idxsp)
	{
		super(id,name);
		
		if(idxsp==null||idxsp.equals(""))
			throw new IllegalArgumentException("invalid idx struct path");
		
		idxStructPath = new XmlDataPath(idxsp) ;
	}
	
	public void update(String name,String idxsp)
	{
		this.setName(name);
		idxStructPath = new XmlDataPath(idxsp) ;
	}

	@Override
	public boolean isMultiValue()
	{
		return true;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof DataXIndexMVSC))
			return false;
		
		if(!super.equals(o))
			return false;
		
		return idxStructPath.equals(((DataXIndexMVSC)o).idxStructPath) ;
	}
	
	public XmlDataPath getIdxStructPath()
	{
		return idxStructPath ;
	}
	
	void setIdxStructPath(String sp)
	{
		if(sp==null)
			throw new IllegalArgumentException("invalid struct path ,it cannot be null or empty!");
		
		XmlDataPath dxp = new XmlDataPath(sp);
		if(dxp.equals(idxStructPath))
			return ;
		
		DataXClass dxc = this.getDataXClass() ;
		XmlDataMember si = dxc.getDataStruct().getXmlDataMemberByPath(dxp.getPath());
		if(si==null)
			throw new IllegalArgumentException("invalid struct path,it cannot be found in XmlDataStruct");
		
		if(!si.isArray())
			throw new IllegalArgumentException("invalid struct path,it not be array!");
		
		idxStructPath = dxp ;
	}
	
	@Override
	public boolean checkValid(XmlDataStruct xds,StringBuffer failedreson)
	{
		if(idxStructPath==null)
		{
			failedreson.append("xml data path is null!");
			return false;
		}
		
		XmlDataMember si = xds.getXmlDataMemberByPath(idxStructPath.getPath());
		if(si==null)
		{
			failedreson.append("cannot find member by path");
			return false;
		}
		
		if(!si.isArray())
		{
			failedreson.append("xml data member is not array!");
			return false;
		}
		
		return true ;
	}
	

	@Override
	public IdxType getIdxType()
	{
		return IdxType.MULTI_VAL_SINGLE_COL;
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
		// TODO Auto-generated method stub
		return null;
	}

	private transient List<JavaColumnAStructItem> columnInfos = null;//new ArrayList<JavaColumnInfo>();
	
	@Override
	protected List<JavaColumnAStructItem> initColumnInfos()
	{
		if(idxStructPath==null)
			throw new RuntimeException("cannot find idx struct path");
		
//		StringTokenizer st = new StringTokenizer(idxStructPath,"/");
//		int c = st.countTokens() ;
//		String[] ps = new String[c];
//		for(int i = 0 ; i < c ; i ++)
//			ps[i] = st.nextToken() ;
		
		XmlDataMember si = dataxClass.getDataStruct().getXmlDataMemberByPath(idxStructPath.getPath()) ;
		if(si==null)
			throw new RuntimeException("cannot find struct item with path="+idxStructPath);
		
		List<JavaColumnAStructItem> rets = new ArrayList<JavaColumnAStructItem>();
		JavaColumnAStructItem jc_si = new JavaColumnAStructItem(idxStructPath,si);
		rets.add(jc_si);
		return rets ;
	}

	public JavaColumnAStructItem getSingleColumnInfo()
	{
		List<JavaColumnAStructItem> jcsis = this.getColumnInfos() ;
		if(jcsis==null||jcsis.size()<=0)
			return null ;
		
		return jcsis.get(0);
	}

	private boolean insertXmlDataIdxIO(Connection conn,long dxid,XmlData xd) throws Exception
	{
		JavaColumnAStructItem jcsi = getSingleColumnInfo() ;
		if(jcsi==null)
			throw new RuntimeException("cannot get single column info,may be not be inited");
		
		PreparedStatement ps = null;

		List mv = extractArrayValFromXmlData(xd, jcsi.getMemberPath().getPathNames());
		if(mv==null||mv.size()<=0)
			return true ;
		
		try
		{
			
			// System.out.println("[addRecord sql]=="+sqlstr.toString());
			ps = conn.prepareStatement(SQL_Insert());
			ps.setObject(1, dxid);
			for(Object v : mv)
			{
				//ps.setObject(2, v);
				if(v==null)
					ps.setNull(2, jcsi.columnInfo.getSqlValType());
				else
					ps.setObject(2, v);
				
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
