package com.dw.db;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import org.w3c.dom.Element;

import com.dw.system.AppConfig;
import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.system.IAppWebConfigLoadedListener;
import com.dw.system.IServerBootComp;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlData;

public class DerbyServer implements IServerBootComp,
		IAppWebConfigLoadedListener
{
	static ILogger log = LoggerManager.getLogger("DerbyServer");
	

	public static final String DERBY_CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver";

	int port = 1527 ;
	
	String dbName = null ;
	String dbPath = null ;
	
	NetworkServerControl nsCtrl = null ;
	
	PrintWriter pw = null ;
	
	
	String clientUrl = null ;
	String clientDriver = DERBY_CLIENT_DRIVER;
	
	String embedUrl = null;
	
	public DerbyServer()
	{
//		Properties properties = new java.util.Properties();
//
//		// The user and password properties are a must, required by JCC
//		properties.setProperty("user", "derbyuser");
//		properties.setProperty("password", "pass");
		
		try
		{
			Element ele = AppConfig.getConfElement("derby_server");
			if (ele != null)
			{
				port = Convert.parseToInt32(ele.getAttribute("port"),1527) ;
				dbName = ele.getAttribute("db_name") ;
				
			}
			
			if(Convert.isNullOrEmpty(dbName))
			{
				dbName = "default_db";
			}
			
			String dataDir = AppConfig.getDataDirBase();//+"derby" ;
			//和data目录并联，自动创建一个data_derby目录
			File dd = new File(dataDir) ;
			File pdf = dd.getParentFile() ;
			File dbfd = new File(pdf,"data_derby") ;
			if(!dbfd.exists())
				dbfd.mkdirs() ;
			
			dbPath = dbfd.getCanonicalPath()+"/"+dbName;
			
			try
			{
				if(port>0)
				{
					nsCtrl = new NetworkServerControl(InetAddress
							.getByName("localhost"), port);
				}
				else
				{
					nsCtrl = new NetworkServerControl() ;
				}
				
				
				//serverControl.
				//pw.println("Derby Network Server created");
				
				
				
				clientUrl = "jdbc:derby://localhost:"
				+ port + "/"+dbPath+";create=true;";
				
				embedUrl = "jdbc:derby:"+dbPath+";";
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception ee)
		{
			ee.printStackTrace();
		}
	}
	
	
	/**
	 * trace utility of server
	 */
	void trace(boolean onoff)
	{
		try
		{
			nsCtrl.trace(onoff);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	public String getBootCompName()
	{
		return "derby_server";
	}

	public void startComp() throws Exception
	{
		pw = new PrintWriter(System.out,true) ;
		pw.print("start derby server:\r\n"+clientUrl) ;
		nsCtrl.start(pw);
	}

	public void stopComp() throws Exception
	{
		nsCtrl.shutdown();
		pw.close() ;
	}

	public boolean isRunning() throws Exception
	{
		try
		{
			nsCtrl.ping();
			return true ;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public void onAppWebConfigLoaded(AppWebConfig awc, ClassLoader comp_cl)
	{

	}
}

