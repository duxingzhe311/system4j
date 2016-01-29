package com.dw.system.gdb.datax;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

import com.dw.system.gdb.db_idx.*;
//import com.dw.system.gdb.datax.db.connpool.IConnPoolToucher;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.gdb.connpool.IConnPoolToucher;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * һ�����������ʹ��һ����ϵ���ݿ��,�����ݿ�����Զ�����
 * 
 * ����XmlData���ݽṹ����������Զ��������ƴ��������ѡ�Ϊ���ܹ������Զ��������ƣ�����
 * �����ݽṹ�Ӳ��ֵ�Լ����ͬʱ���ܹ���֤�㹻��������֧��ҵ��Ŀ������ڴ˼�����Լ����
 * 1���ܹ����������������������Ҳ������XmlData�ڲ�ֻ���ṩ��ֵ�����������ж���
 * 2����XmlData�����ṩ ��ֵ������
 * 
 * ������Լ�����Զ�����������������
 * 1,��ֵ���� SINGLE_VAL_MULTI_COL
 * 	
 * 2,��ֵ���� MULTI_VAL_SINGLE_COL
 * 	��Ҫ�������ֵ
 * 3,��ֵ���� MULTI_VAL_MULTI_COL
 * 
 * 
 * ���ӣ�
 * @author Jason Zhu
 */

public abstract class DataXIndex implements IXmlDataable,IConnPoolToucher
{
	public enum IdxType
	{
		SINGLE_VAL_MULTI_COL,
		MULTI_VAL_SINGLE_COL,
		MULTI_VAL_MULTI_COL
	}
	
	
	private static ILogger log = LoggerManager.getLogger(DataXIndex.class.getCanonicalName());
	
	
	public static class JavaColumnAStructItem
	{
		String colPrefix = "" ;
		
		XmlDataPath memberPath = null ;
		XmlDataMember structItem = null ;
		JavaColumnInfo columnInfo = null ;
		
		public JavaColumnAStructItem(XmlDataPath dxp,XmlDataMember si)
		{
			this("",dxp,si,true);
		}
		
		public JavaColumnAStructItem(String colprefix,XmlDataPath dxp,XmlDataMember si,
				boolean hasidx)
		{
			if(colprefix!=null)
				colPrefix = colprefix ;
			
			memberPath = dxp ;
			
			structItem = si ;
			
			columnInfo = new JavaColumnInfo(colPrefix+memberPath.toColumnName(),false,XmlVal.StrType2ValType(si.getValType()),
					si.getMaxLen(),hasidx,false,false,-1);
		}
		
		public JavaColumnAStructItem(String path,XmlDataMember si)
		{
			this("",new XmlDataPath(path),si,true);
		}
		
		public XmlDataPath getMemberPath()
		{
			return memberPath ;
		}
	}

	
	public static XmlData transToXmlData(DataXIndex dxi)
	{
		XmlData xd = dxi.toXmlData();
		
		if(dxi instanceof DataXIndexSVMC)
		{
			xd.setParamValue("idx_type", DataXIndex.IdxType.SINGLE_VAL_MULTI_COL.toString());
		}
		else if(dxi instanceof DataXIndexMVSC)
		{
			xd.setParamValue("idx_type", DataXIndex.IdxType.MULTI_VAL_SINGLE_COL.toString());
		}
		else if(dxi instanceof DataXIndexMVMC)
		{
			xd.setParamValue("idx_type", DataXIndex.IdxType.MULTI_VAL_MULTI_COL.toString());
		}
		return xd;
	}
	
	public static DataXIndex transFromXmlData(XmlData xd)
	{
		String typestr = xd.getParamValueStr("idx_type");
		DataXIndex.IdxType idxt = DataXIndex.IdxType.valueOf(typestr);
		
		DataXIndex dxi = null ;
		
		if(idxt==DataXIndex.IdxType.SINGLE_VAL_MULTI_COL)
		{
			dxi = new DataXIndexSVMC() ;
		}
		else if(idxt==DataXIndex.IdxType.MULTI_VAL_SINGLE_COL)
		{
			dxi = new DataXIndexMVSC() ;
		}
		else if(idxt==DataXIndex.IdxType.MULTI_VAL_MULTI_COL)
		{
			dxi = new DataXIndexMVMC() ;
		}
		else
		{
			throw new RuntimeException("unknow idx type="+typestr);
		}
		
		dxi.fromXmlData(xd) ;
		
		return dxi ;
	}
	
	//private ArrayList<DataXIndexItem> dataXII = new ArrayList<DataXIndexItem>();
	int id = -1 ;
	String name = null ;
	/**
	 * ������·��������XmlDataStruct����ȡ����
	 */
	
	
	protected transient DataXBase dataxBase = null ;
	protected transient DataXClass dataxClass = null ;
	
//	DataXIndex(DataXBase dxb,DataXClass xdc)
//	{
//		dataxBase = dxb ;
//		dataxClass = xdc ;
//	}
	
	public DataXIndex()
	{}
	
	public DataXIndex(int id,String name)
	{
//		dataxBase = dxb ;
//		dataxClass = xdc ;
		this.id = id ;
		this.name = name ;
	}
	
	public String toString()
	{
		return name ;
	}
	
	protected transient JavaTableInfo javaTableInfo = null ;
	protected transient List<JavaColumnAStructItem> columnInfos= null ;
	
	void init(DataXBase dxb,DataXClass dxc) throws Exception
	{
		dataxBase = dxb ;
		dataxClass = dxc ;
		
		JavaColumnInfo pkcol = null ;
		List<JavaColumnInfo> norcols = new ArrayList<JavaColumnInfo>();
		if(isMultiValue())
		{
			pkcol = new JavaColumnInfo("auto_id",true,XmlVal.XmlValType.vt_int64,
					-1,true,true,true,-1);
			
			
			norcols.add(new JavaColumnInfo("xd_id",false,XmlVal.XmlValType.vt_int64,
					-1,true,false,false,-1));
		}
		else
		{
			pkcol = new JavaColumnInfo("xd_id",true,XmlVal.XmlValType.vt_int64,
					-1,true,true,false,-1);
		}
		
		List<JavaColumnAStructItem> jcsis = initColumnInfos() ;
		for(JavaColumnAStructItem jcsi:jcsis)
		{
			norcols.add(jcsi.columnInfo);
		}
		
		ArrayList<JavaForeignKeyInfo> jfkis = new ArrayList<JavaForeignKeyInfo>();
		jfkis.add(new JavaForeignKeyInfo("xd_id",dataxClass.getTableName(),"xd_id"));
		javaTableInfo = new JavaTableInfo(getTableName(),pkcol,norcols,jfkis);
		//return javaTableInfo;
		
		columnInfos = jcsis;
		
		dataxBase.getDatabase().getConnPool().putToucher(this);
	}
	
	
	public void OnMeBeTouched()
	{
		try
		{
			dataxBase.initNotExistedTable(javaTableInfo) ;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
		}
	}
	
	protected abstract List<JavaColumnAStructItem> initColumnInfos();
	
	public JavaTableInfo getJavaTableInfo()
	{
		return javaTableInfo;
	}
	
	public List<JavaColumnAStructItem> getColumnInfos()
	{
		return columnInfos;
	}
	
	public DataXBase getDataXBase()
	{
		return dataxBase ;
	}
	
	public DataXClass getDataXClass()
	{
		return dataxClass ;
	}
	
	public int getId()
	{
		return id ;
	}
	
	void setName(String n)
	{
		name = n ;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getTableName()
	{
		return "dx_"+dataxClass.getId()+"_"+id;
	}
	
	
	
	
	public static Object extractSingleValFromXmlData(XmlData xd,String[] path)
	{
		XmlData tmpxd = xd ;
		for(int i = 0 ; i < path.length - 1 ; i ++)
		{
			tmpxd = tmpxd.getSubDataSingle(path[i]);
			if(tmpxd==null)
				throw new IllegalArgumentException("cannot get single sub xml data with name="+path[i]);
		}
		
		return tmpxd.getParamValue(path[path.length-1]);
	}
	
	
	public static List extractArrayValFromXmlData(XmlData xd,String[] path)
	{
		XmlData tmpxd = xd ;
		for(int i = 0 ; i < path.length - 1 ; i ++)
		{
			tmpxd = tmpxd.getSubDataSingle(path[i]);
			if(tmpxd==null)
				throw new IllegalArgumentException("cannot get single sub xml data with name="+path[i]);
		}
		
		return tmpxd.getParamValues(path[path.length-1]);
	}
	
	
	public static List<XmlData> extractSubArrayXmlDataByPath(XmlData xd,String[] path)
	{
		XmlData tmpxd = xd ;
		for(int i = 0 ; i < path.length - 1 ; i ++)
		{
			tmpxd = tmpxd.getSubDataSingle(path[i]);
			if(tmpxd==null)
				throw new IllegalArgumentException("cannot get single sub xml data with name="+path[i]);
		}
		
		return tmpxd.getSubDataArray(path[path.length-1]);
	}
	
	public boolean equals(Object o)
	{
		DataXIndex dxi = (DataXIndex)o;
		
		if(id!=dxi.id)
			return false;
		if(!name.equals(dxi.name))
			return false;
		
		return true ;
	}
	
	
	public abstract boolean checkValid(XmlDataStruct xds,StringBuffer failedreson);
	
	
	public abstract IdxType getIdxType();
//	public DataXIndexItem[] getDataXIndexItem()
//	{
//		DataXIndexItem[] dxii = new DataXIndexItem[dataXII.size()];
//		dataXII.toArray(dxii);
//		return dxii ;
//	}
	
	public abstract boolean isMultiValue();
	
	/**
	 * ����ʵ��ʱ���������������XmlData����ȡ��Ϣ�����뵽��Ӧ����������
	 * ���ԭ�������Ѿ����ڣ�������ɾ��
	 * @param xdid
	 * @param xd
	 */
	public abstract void OnSetXmlData(Connection conn,long xdid,XmlData xd)
		throws Exception;
	
	/**
	 * ��һ��XmlData��ɾ��ʱӦ��������
	 * @param xdid
	 */
	public void OnDelXmlData(Connection conn,long xdid)
		throws Exception
	{
		delIdxIO(conn,xdid);
	}
	
	/**
	 * �ڱ������ڲ��������ṩ����
	 * @param cond_str
	 * @param pageidx
	 * @param pagesize
	 * @return
	 */
	public abstract long[] findByCondition(String cond_str,int pageidx,int pagesize);
	
	
	
	////////////////////
	protected boolean delIdxIO(Connection conn,long did) throws Exception
	{
		PreparedStatement stat = null;
		try
		{
			String sql = "delete from " +getTableName()+" where xd_id=?";
			// System.out.println("[readSingleRecord sql]==" + sql);
			stat = conn.prepareStatement(sql);
			stat.setObject(1, did);
			return stat.execute();
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}
	
	
	protected String SQL_Insert()
	{
		StringBuffer tmpsb = new StringBuffer();
		
		List<JavaColumnAStructItem> cols = this.getColumnInfos();
		
		tmpsb.append("insert into ").append(getTableName())
			.append(" (xd_id");
		for(JavaColumnAStructItem jci:cols)
		{
			tmpsb.append(",").append(jci.columnInfo.getColumnName());
		}
		tmpsb.append(") values (?");
		for(JavaColumnAStructItem jci:cols)
		{
			tmpsb.append(",?");
		}
		tmpsb.append(")");
		return tmpsb.toString();
	}
	
	 
	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("id", id);
		xd.setParamValue("name", name);
		return xd ;
	}
	
	public void fromXmlData(XmlData xd)
	{
		id = xd.getParamValueInt32("id", -1);
		name = xd.getParamValueStr("name");
	}
}