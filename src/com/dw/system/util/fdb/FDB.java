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
 * 在很多情况下需要使用如下文件：
 * 
 * 	按照一定的规律写入不同大小的数据块，每个数据块有一个索引值。
 *  系统还可以根据索引值读取对应的数据块内容。并做删除标记
 *  
 *  不同进程间，可以通过此文件进行数据交换
 *  
 *  
 *  文件
 *  
 * @author Jason Zhu
 */
public class FDB
{
	/**
	 * 在一个进程内，每个文件的操作只有一个对象
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
	 * 主键key到数据头的映射
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
	 * 检查表是否存在，如果不存在，则创建
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
