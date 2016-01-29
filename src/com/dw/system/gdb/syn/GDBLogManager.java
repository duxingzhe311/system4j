package com.dw.system.gdb.syn;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import com.dw.system.AppConfig;
import com.dw.system.gdb.xorm.XORMClass;

/**
 * �Է�����־���м�¼�Ĺ���������
 * 
 * ������־�Ա�Ϊ��λ���д����������Ϳ��Զ���Ҫ������־��¼�ı����˧ѡ����
 * 
 * ��Ҫ��¼�Ա�ĸ��²��������ӣ�ɾ�����޸ģ�
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
	 * �洢����ϵͳ�У���Ҫ������־��¼�ı���Ϣ
	 * ֻҪ��Ӧ����Ϣ���ڣ�GDB�����˱�ĸ��²���ʱ�������¼����־
	 * 
	 * ��ӳ����Proxy�˺�Server�˵����ݿ��ܸ���ͬ����־����Ҫ��������ͬ
	 */
	HashMap<String,GDBLogTable> table2LogTable = new HashMap<String,GDBLogTable>() ;
	
	/**
	 * ��¼���еı����ʵ�XORM���ӳ��-����һ��ȫ����Proxy��Server���п����õ���
	 * ���ݱ���������ص����ݲ�������Server���ݴ���Ϣ������GDB��������ȡĳһ�����е��ض�
	 * ���ݸ�Proxy
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
		{//mode2 Server����־
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
		{//mode2 Server����־
			mode3Table2Xormc.put(tn, xormc);
			if(AppConfig.isRunAsDistributedProxy())
			{
				GDBLogTable lt = new GDBLogTable(xormc,modulename,tn,-1,-1);
				//String uid = modulename+"."+tablename.toLowerCase() ;
				table2LogTable.put(tn, lt) ;
			}
			else
			{//server�������޵�7�����־
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
	 * ���ݱ����ʻ�ö�ӦXORMC
	 * @param tablename
	 * @return
	 */
	public Class getXORMClassByTableName(String tablename)
	{
		return allTable2Xormc.get(tablename) ;
	}
}
