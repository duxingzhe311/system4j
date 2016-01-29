package com.dw.system.gdb.syn;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import com.dw.system.AppConfig;
import com.dw.system.gdb.xorm.XORMClass;

/**
 * 对访问日志进行记录的管理器主类
 * 
 * 访问日志以表为单位进行处理——这样就可以对需要进行日志记录的表进行帅选控制
 * 
 * 主要记录对表的更新操作（增加，删除，修改）
 * 
 * 
 * @author Jason Zhu
 */
public class GDBLogManager
{
	static Object locker = new Object() ;
	static GDBLogManager ins = null ;
	
	public static GDBLogManager getInstance()
	{
		if(ins!=null)
			return ins ;
		
		synchronized(locker)
		{
			if(ins!=null)
				return ins ;
			
			ins = new GDBLogManager() ;
			return ins ;
		}
	}
	
	static boolean isDerbyStarted = false;
	
	public synchronized static void startDerby() throws Exception
	{
		if(isDerbyStarted)
			return ;
		
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver).newInstance();
		
		Connection conn = getDerbyConn() ;
		isDerbyStarted =conn!=null;
		
		conn.close() ;
	}
	
	public static void startDistributedSyn() throws Exception
	{
		startDerby();
		
		DistributedHelper.startProxySynMode1() ;
		
		DistributedHelper.startProxySynMode2() ;
	}
	
	static String getGDBLogFileDir()
	{
		return AppConfig.getDataDirBase()+"gdb_log/" ;
	}
	
	public static void shutDerby() throws SQLException
	{
		DriverManager.getConnection("jdbc:derby:"+getGDBLogFileDir()+";shutdown=true");
	}
	
	static Connection getDerbyConn() throws Exception
	{
		//startDerby();
		
		Properties props  = new  Properties() ;
		return DriverManager.getConnection("jdbc:derby:"+getGDBLogFileDir()+";create=true", props);
	}
	
	/**
	 * 存储本地系统中，需要进行日志记录的表信息
	 * 只要对应的信息存在，GDB在做此表的更新操作时，都会记录其日志
	 * 
	 * 改映射在Proxy端和Server端的内容可能更加同步日志的需要会有所不同
	 */
	HashMap<String,GDBLogTable> table2LogTable = new HashMap<String,GDBLogTable>() ;
	
	/**
	 * 记录所有的表名词到XORM类的映射-它是一个全集，Proxy和Server都有可能用到它
	 * 根据表名称做相关的数据操作。如Server根据此信息，调用GDB方法，读取某一个表中的特定
	 * 内容给Proxy
	 */
	HashMap<String,Class> allTable2Xormc = new HashMap<String,Class>() ;
	
	HashMap<String,Class> mode1Table2Xormc = new HashMap<String,Class>() ;
	
	HashMap<String,Class> mode2Table2Xormc = new HashMap<String,Class>() ;
	
	HashMap<String,Class> mode3Table2Xormc = new HashMap<String,Class>() ;
	
	private GDBLogManager()
	{
		
	}
	
	
	public void setLogTable(String modulename,Class xormc,String table) throws Exception
	{
		XORMClass xormCC = (XORMClass)  xormc.getAnnotation(XORMClass.class);
		if(xormCC==null)
			return ;
		
		int dm = xormCC.distributed_mode();
		if(dm<=0)
			return ;
		
		String tn = table.toLowerCase() ;
		
		allTable2Xormc.put(tn, xormc) ;
		
		if(dm==1)
		{
			mode1Table2Xormc.put(tn, xormc);
			if(AppConfig.isRunAsDistributedProxy())
			{
				GDBLogTable lt = new GDBLogTable(xormc,modulename,tn,-1,-1);
				//String uid = modulename+"."+tablename.toLowerCase() ;
				table2LogTable.put(tn, lt) ;
			}
			return ;
		}
		//
		
		if(dm==2)
		{//mode2 Server做日志
			mode2Table2Xormc.put(tn, xormc);
			if(!AppConfig.isRunAsDistributedProxy())
			{
				GDBLogTable lt = new GDBLogTable(xormc,modulename,tn,20,-1);
				//String uid = modulename+"."+tablename.toLowerCase() ;
				table2LogTable.put(tn, lt) ;
			}
			return ;
		}
		
		if(dm==3)
		{//mode2 Server做日志
			mode3Table2Xormc.put(tn, xormc);
			if(AppConfig.isRunAsDistributedProxy())
			{
				GDBLogTable lt = new GDBLogTable(xormc,modulename,tn,-1,-1);
				//String uid = modulename+"."+tablename.toLowerCase() ;
				table2LogTable.put(tn, lt) ;
			}
			else
			{//server端做有限的7天的日志
				GDBLogTable lt = new GDBLogTable(xormc,modulename,tn,-1,7*24*3600000l);
				//String uid = modulename+"."+tablename.toLowerCase() ;
				table2LogTable.put(tn, lt) ;
			}
			return ;
		}
		
	}
	
	public GDBLogTable getLogTable(String tablename)
	{
		//String uid = dbname+"."+tablename.toLowerCase() ;
		return table2LogTable.get(tablename.toLowerCase()) ;
	}
	
	public ArrayList<GDBLogTable> getAllLogTables()
	{
		ArrayList<GDBLogTable> rets = new ArrayList<GDBLogTable>() ;
		for(GDBLogTable lt:table2LogTable.values())
			rets.add(lt) ;
		return rets ;
	}
	
	public ArrayList<String> getAllMode1Tables()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		rets.addAll(this.mode1Table2Xormc.keySet()) ;
		return rets ;
	}
	
	public ArrayList<String> getAllMode2Tables()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		rets.addAll(this.mode2Table2Xormc.keySet()) ;
		return rets ;
	}
	
	public ArrayList<String> getAllMode3Tables()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		rets.addAll(this.mode3Table2Xormc.keySet()) ;
		return rets ;
	}
	
	public Class getMode1TableClass(String tn)
	{
		return this.mode1Table2Xormc.get(tn.toLowerCase()) ;
	}
	
	public Class getMode2TableClass(String tn)
	{
		return this.mode2Table2Xormc.get(tn.toLowerCase()) ;
	}
	
	public Class getMode3TableClass(String tn)
	{
		return this.mode3Table2Xormc.get(tn.toLowerCase()) ;
	}
	/**
	 * 根据表名词获得对应XORMC
	 * @param tablename
	 * @return
	 */
	public Class getXORMClassByTableName(String tablename)
	{
		return allTable2Xormc.get(tablename) ;
	}
}
