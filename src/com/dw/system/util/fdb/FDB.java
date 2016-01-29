package com.dw.system.util.fdb;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import com.dw.system.AppConfig;
/**
 * �ںܶ��������Ҫʹ�������ļ���
 * 
 * 	����һ���Ĺ���д�벻ͬ��С�����ݿ飬ÿ�����ݿ���һ������ֵ��
 *  ϵͳ�����Ը�������ֵ��ȡ��Ӧ�����ݿ����ݡ�����ɾ�����
 *  
 *  ��ͬ���̼䣬����ͨ�����ļ��������ݽ���
 *  
 *  
 *  �ļ�
 *  
 * @author Jason Zhu
 */
public class FDB
{
	/**
	 * ��һ�������ڣ�ÿ���ļ��Ĳ���ֻ��һ������
	 * @param filepath
	 * @return
	 */
	public static FDB getFDBByFilePath(String filepath)
	{
		return null ;
	}
	
	static boolean isDerbyStarted = false;
	
	static void startDerby() throws Exception
	{
		if(isDerbyStarted)
			return ;
		
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver).newInstance();
		
		isDerbyStarted =true;
	}
	
	static String getGDBLogFileDir()
	{
		return AppConfig.getDataDirBase()+"gdb_log/" ;
	}
	
	static void shutDerby() throws SQLException
	{
		DriverManager.getConnection("jdbc:derby:"+getGDBLogFileDir()+";shutdown=true");
	}
	
	static Connection getDerbyConn() throws SQLException
	{
		Properties props  = new  Properties() ;
		return DriverManager.getConnection("jdbc:derby:"+getGDBLogFileDir()+";create=true", props);
	}
	
	RandomAccessFile raf = null ;
	FileChannel fileC = null ;
	
	/**
	 * ����key������ͷ��ӳ��
	 */
	HashMap<String,FDBDataHeader> key2datahead = new HashMap<String,FDBDataHeader>() ;
	
	Connection derbyConn = null ;
	
	private FDB(String filepath) throws Exception
	{
		startDerby() ;
		
		File f = new File(filepath) ;
		if(!f.getParentFile().exists())
			f.getParentFile().mkdirs() ;
		
		raf = new RandomAccessFile(filepath,"rw");
		
		fileC = raf.getChannel() ;
		
		derbyConn = getDerbyConn() ;
	}
	
	
	/**
	 * �����Ƿ���ڣ���������ڣ��򴴽�
	 */
	void init()
	{
		
	}
	
	//public 
	
	public void close() throws IOException
	{
		if(fileC!=null)
		{
			fileC.close() ;
		}
		
		if(raf!=null)
		{
			raf.close() ;
		}
	}
}
