package com.dw.system.gdb.conf.autofit;

import java.io.*;
import java.util.*;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.*;

/**
 * 用java信息描述的数据库表信息
 * 
 * 他可以提供和具体数据库无关的表信息。Database可以根据该信息针对不同的数据库做
 * 数据库的表自动建立，索引的自动建立等工作
 * @author Jason Zhu
 */
public class JavaTableInfo implements IXmlDataable
{
	private String tableName = null ;
	private JavaColumnInfo pkColInfo = null ;
	private List<JavaColumnInfo> norColInfos = null ;
	private List<JavaForeignKeyInfo> fkInfos = null ;
	
	public JavaTableInfo()
	{
		
	}
	
	public JavaTableInfo(String tablen,
			JavaColumnInfo pkcol,List<JavaColumnInfo> cols,
			List<JavaForeignKeyInfo> fkinfos)
	{
		tableName = tablen ;
		pkColInfo = pkcol ;
		norColInfos = cols ;
		fkInfos = fkinfos ;
	}
	
	public String getTableName()
	{
		return tableName ;
	}
	
	public void setTableName(String tn)
	{
		tableName = tn ;
	}
	
	public JavaColumnInfo getPkColumnInfo()
	{
		return pkColInfo ;
	}
	
	public JavaColumnInfo[] getNorColumnInfos()
	{
		if(norColInfos==null)
			return new JavaColumnInfo[0];
		
		JavaColumnInfo[] rets = new JavaColumnInfo[norColInfos.size()];
		norColInfos.toArray(rets);
		return rets ;
	}
	
	public JavaColumnInfo getColumnInfoByName(String n)
	{
		if(pkColInfo.getColumnName().equalsIgnoreCase(n))
			return pkColInfo ;
		
		for(JavaColumnInfo jci:norColInfos)
		{
			if(jci.getColumnName().equalsIgnoreCase(n))
				return jci ;
		}
		
		return null ;
	}
	
	JavaColumnInfo[] updateNorCols = null ;
	
	public JavaColumnInfo[] getUpdateNorColumnInfos()
	{
		if(updateNorCols!=null)
			return updateNorCols ;
		
		if(norColInfos==null)
		{
			updateNorCols = new JavaColumnInfo[0];
			return updateNorCols ;
		}
		
		ArrayList<JavaColumnInfo> rets = new ArrayList<JavaColumnInfo>() ;
		for(JavaColumnInfo jci:norColInfos)
		{
			if(jci.isUpdateAsSingle())
				continue ;
			
			rets.add(jci) ;
		}
		
		JavaColumnInfo[] ss = new JavaColumnInfo[rets.size()];
		rets.toArray(ss);
		updateNorCols = ss ;
		return updateNorCols ;
	}
	
	public JavaColumnInfo getNorColumnInfo(String n)
	{
		if(norColInfos==null)
			return null ;
		
		for(JavaColumnInfo jci:norColInfos)
		{
			if(jci.getColumnName().equalsIgnoreCase(n))
				return jci ;
		}
		return null ;
	}
	
	public JavaForeignKeyInfo[] getForeignKeyInfos()
	{
		if(fkInfos==null)
			return new JavaForeignKeyInfo[0];
		
		JavaForeignKeyInfo[] rets = new JavaForeignKeyInfo[fkInfos.size()];
		fkInfos.toArray(rets);
		return rets ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("table_name", tableName);
		if(pkColInfo!=null)
		{
			xd.setSubDataSingle("pk_col", pkColInfo.toXmlData());
		}
		
		if(norColInfos!=null)
		{
			List<XmlData> xds = xd.getOrCreateSubDataArray("nor_cols");
			for(JavaColumnInfo jci:norColInfos)
			{
				xds.add(jci.toXmlData());
			}
		}
		
		if(fkInfos!=null)
		{
			List<XmlData> xds = xd.getOrCreateSubDataArray("fks");
			for(JavaForeignKeyInfo fki:fkInfos)
			{
				xds.add(fki.toXmlData());
			}
		}
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		tableName = xd.getParamValueStr("table_name");
		XmlData tmpxd = xd.getSubDataSingle("pk_col") ;
		
		if(tmpxd!=null)
		{
			pkColInfo = new JavaColumnInfo();
			pkColInfo.fromXmlData(tmpxd);
		}
		
		List<XmlData> tmpxds = xd.getSubDataArray("nor_cols");
		if(tmpxds!=null)
		{
			norColInfos = new ArrayList<JavaColumnInfo>(tmpxds.size());
			for(XmlData xd0:tmpxds)
			{
				JavaColumnInfo tmpjci = new JavaColumnInfo();
				tmpjci.fromXmlData(xd0);
				norColInfos.add(tmpjci);
			}
		}
		
		tmpxds = xd.getSubDataArray("fks");
		if(tmpxds!=null)
		{
			fkInfos = new ArrayList<JavaForeignKeyInfo>(tmpxds.size());
			for(XmlData xd0:tmpxds)
			{
				JavaForeignKeyInfo tmpfki = new JavaForeignKeyInfo();
				tmpfki.fromXmlData(xd0);
				fkInfos.add(tmpfki);
			}
		}
	}
}
