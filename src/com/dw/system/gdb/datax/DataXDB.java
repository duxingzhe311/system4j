package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.ConnPoolMgr;
import com.dw.system.gdb.conf.DBType;
import com.dw.system.gdb.connpool.DBInfo;
import com.dw.system.gdb.db_idx.*;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * 统一存放数据库连接信息（连接池）
 * @author Jason Zhu
 */
@XRmi(reg_name = "datax_db")
public class DataXDB implements IXmlDataable
{
	int defaultDBInfoIdx = 0 ;
	/**
	 * 所有的DBInfo列表
	 */
	ArrayList<DBInfo> allInfos = new ArrayList<DBInfo>() ;
	
	/**
	 * 名字到DbInfo的映射，一个DBInfo可以有多个名字与之对应
	 * 里面的DBInfo必须是已经定义好的内容
	 */
	Hashtable<String,DBInfo> dbname2Info = new Hashtable<String,DBInfo>();
	
	//transient String hsqlBase = null ;
	
	transient Hashtable<DBInfo,Database> dbinfo2db = new Hashtable<DBInfo,Database>() ;
	
	public DataXDB() throws Exception
	{
		this("");
	}
	/**
	 * 构造一个DataXDB对象，其内部必然存在一个数据库连接
	 * 该内部数据库连接不做保存
	 * @param hsqlbase
	 * @throws Exception 
	 */
	public DataXDB(String hsqlbase) throws Exception
	{
		//hsqlBase = hsqlbase ;
		
		//生成缺省的
		DBInfo innerDBInfo = ConnPoolMgr.getDefaultConnPool().getDBInfo();//new DBInfo();
		
		
		//第0个必定是内部自带的数据库连接
		allInfos.add(innerDBInfo);
	}
	
//	public DataXDB(String hsqlbase)
//	{
//		//hsqlBase = hsqlbase ;
//		
//		//生成缺省的
//		DBInfo innerDBInfo = new DBInfo();
//		innerDBInfo.dbType = DBType.hsql;
//		Properties p = innerDBInfo.connProp;
//		p.setProperty("db.driver", "org.hsqldb.jdbcDriver");
//		// 使用id作为数据库名称
//		p.setProperty("db.url", "jdbc:hsqldb:file:"
//				+ hsqlbase + "inner");
//		p.setProperty("db.username", "sa");
//		p.setProperty("db.password", "");
//		p.setProperty("db.initnumber", "2");
//		p.setProperty("db.maxnumber", "5");
//		
//		//第0个必定是内部自带的数据库连接
//		allInfos.add(innerDBInfo);
//	}
	
	
	public DBInfo[] getAllDBInfos()
	{
		DBInfo[] dbis = new DBInfo[allInfos.size()];
		allInfos.toArray(dbis);
		return dbis ;
	}
	
	public void setDBInfo(DBInfo dbi)
	{
//		if(allInfos.contains(dbi))
//			throw new IllegalArgumentException("already contains same db connection!");
		
		int p = allInfos.indexOf(dbi);
		if(p<0)
		{
			allInfos.add(dbi);
			return;
		}
		
		if(p==0)
			throw new IllegalArgumentException("inner default connection cannot be changed!");
		
		allInfos.set(p, dbi);
	}
	
	public int getDBInfoNum()
	{
		return allInfos.size();
	}
	
	public int indexOf(DBInfo dbi)
	{
		int c = allInfos.size();
		for(int i = 0 ; i < c ; i ++)
		{
			if(dbi.equals(allInfos.get(i)))
				return i;
		}
		
		return -1 ;
	}
	
	public String[] getAlias(DBInfo dbi)
	{
		ArrayList<String> ss = new ArrayList<String>() ;
		for(String n:dbname2Info.keySet())
		{
			DBInfo tmpdbi = dbname2Info.get(n);
			if(tmpdbi.equals(dbi))
				ss.add(n) ;
		}
		
		String[] rets = new String[ss.size()];
		ss.toArray(rets);
		return rets ;
	}
	
	public void setDefaultDBInfo(int idx)
	{
		if(idx<0||idx>=allInfos.size())
			throw new IllegalArgumentException("invalid idx");
		
		defaultDBInfoIdx = idx ;
	}
	
	public DBInfo getDefaultDBInfo()
	{
		return allInfos.get(defaultDBInfoIdx);
	}
	
	public boolean isDefaultDBInfo(DBInfo dbi)
	{
		int p = indexOf(dbi);
		if(p<0)
			return false ;
		
		return p==defaultDBInfoIdx;
	}
	/**
	 * 对数据库连接信息设置匿名
	 * @param idx
	 * @param name
	 */
	public void setAlias(int idx,String name)
	{
		if(idx<0||idx>=allInfos.size())
			throw new IllegalArgumentException("invalid idx");
		
		DBInfo dbi = allInfos.get(idx);
		dbname2Info.put(name, dbi);
	}
	
	public void unsetDBInfo(int idx)
	{
		if(idx<1)
			throw new IllegalArgumentException("inner existed db connection cannot be unset");
		
		if(idx>=allInfos.size())
			throw new IllegalArgumentException("out of all connection num!");
		
		if(idx==defaultDBInfoIdx)
			throw new IllegalArgumentException("default db connection cannot be unset");
		
		//删除相关信息
		DBInfo dbi = allInfos.remove(idx);
		if(idx<defaultDBInfoIdx)
		{
			defaultDBInfoIdx -- ;
		}
		//删除匿名
		ArrayList<String> relatedns = new ArrayList<String>() ;
		for(String n:dbname2Info.keySet())
		{
			DBInfo tmpdbi = dbname2Info.get(n);
			if(tmpdbi.equals(dbi))
				relatedns.add(n) ;
		}
		for(String n:relatedns)
		{
			dbname2Info.remove(n);
		}
		//删除关闭已经创建的数据库连接
		Database db = dbinfo2db.get(dbi);
		if(db!=null)
			db.close();
	}
	
	/**
	 * 根据数据名称获得对应的数据库对象，如果直接找到对应信息，则返回，如果没找到，则返回
	 * 缺省数据库对应的Database对象
	 * @param name
	 * @return
	 * @throws Exception
	 */
//	public Database getDatabaseByName(String name) throws Exception
//	{
//		DBInfo dbi = null;
//		if(name!=null)
//			dbi = dbname2Info.get(name);
//		
//		if(dbi==null)
//			dbi = getDefaultDBInfo();
//
//		Database db = dbinfo2db.get(dbi);
//		if(db!=null)
//			return db ;
//		
//		synchronized(this)
//		{
//			//try to get again
//			db = dbinfo2db.get(dbi);
//			if(db!=null)
//				return db ;
//			
//			db = new Database(dbi);
//			dbinfo2db.put(dbi,db);
//			return db;
//		}
//	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("default_idx", defaultDBInfoIdx);
		
		//输出第0个连接的匿名
		DBInfo dbi0 = allInfos.get(0);
		ArrayList<String> relatedns = new ArrayList<String>() ;
		for(String n:dbname2Info.keySet())
		{
			DBInfo tmpdbi = dbname2Info.get(n);
			if(tmpdbi.equals(dbi0))
				relatedns.add(n) ;
		}
		xd.setParamValues("inner_related_names", relatedns);
		
		int c = allInfos.size() ;
		if(c>1)
		{
			List<XmlData> xds = xd.getOrCreateSubDataArray("dbinfos");
			for(int i = 1 ; i < c ; i ++)
			{//第0个连接是内部自带的，不需要进行转换存储
				DBInfo dbi = allInfos.get(i);
				XmlData tmpxd = dbi.toXmlData() ;
				
				//加入指定的名称
				String relatedns_str = "" ;
				for(String n:dbname2Info.keySet())
				{
					DBInfo tmpdbi = dbname2Info.get(n);
					if(tmpdbi.equals(dbi))
						relatedns_str+=(','+n);
				}
				tmpxd.setParamValue("related_names", relatedns_str);
				
				xds.add(tmpxd);
			}
		}
		
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		defaultDBInfoIdx = xd.getParamValueInt32("default_idx", 0);
		String[] innerrns = xd.getParamValuesStr("inner_related_names");
		if(innerrns!=null)
		{//恢复第0个连接的匿名
			DBInfo dbi0 = allInfos.get(0);
			for(String s:innerrns)
			{
				dbname2Info.put(s, dbi0);
			}
		}
		
		List<XmlData> xds = xd.getSubDataArray("dbinfos") ;
		if(xds!=null)
		{
			for(XmlData tmpxd:xds)
			{
				DBInfo dbi = new DBInfo();
				dbi.fromXmlData(tmpxd);
				allInfos.add(dbi);
				
				String relatedns_str = tmpxd.getParamValueStr("related_names");
				if(relatedns_str!=null)
				{
					String[] tmps = relatedns_str.split(",");
					for(String s:tmps)
					{
						String s0 = s.trim();
						if(s0.equals(""))
							continue ;
						
						dbname2Info.put(s0, dbi);
					}
				}
			}
		}
	}
	
	public void close()
	{
		for(Database db:dbinfo2db.values())
		{
			db.close();
		}
	}
}
