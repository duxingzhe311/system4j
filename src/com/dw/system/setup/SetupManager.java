package com.dw.system.setup;

import java.io.*;
import java.util.*;

import com.dw.system.*;
import com.dw.system.xmldata.*;


/**
 * ��ϵͳ��Ϊһ��������ʱ����Ҫ����ܶలװ���õ�������Ϣ
 * 
 * �磺�������˿ںţ�����Ա���룬��װ·����
 * 
 * �ر��ǹ���Ա����-��Ϊ��װ���õ�ʱ�����ݿⲢû�������������õ�����Ӧ������������
 * ���ң���ϵͳ�����󣬱����õ�������Ȼ��Ч--��Ҫ�޸��û�ϵͳ����֤����������root��¼ʱ
 * Ҫ��ȡ��װ�����е����ݡ�
 * 
 * @author Jason Zhu
 */
public class SetupManager implements IXmlDataable
{
	public static final int DEF_MYSQL_PORT = 33306 ;
	public static final int DEF_HTTP_PORT = 9999 ;
	public static final String SETUP_FILENAME = "sys_setup.xml" ;
	static SetupManager inst = null ;
	
	public static SetupManager getInstance()
	{
		if(inst!=null)
			return inst ;
		
		synchronized(SetupManager.class)
		{
			if(inst!=null)
				return inst ;
			
			File f = new File(AppConfig.getDataDirBase()+SETUP_FILENAME) ;
			inst = new SetupManager(f);
			return inst ;
		}
	}
	
	
	File confFile = null ;
	
	String domain = null ;
	int httpPort = DEF_HTTP_PORT ;
	int mySqlPort = DEF_MYSQL_PORT ;
	String rootPsw = null ;
	boolean rootPswToDB = false;
	
	/**
	 * 
	 * @param f
	 */
	public SetupManager(File f)
	{
		confFile = f ;
		
		loadConf();
	}
	
	private void loadConf()
	{
		try
		{
			XmlData xd = XmlData.readFromFile(confFile) ;
			if(xd==null)
				return ;
			
			this.fromXmlData(xd) ;
		}
		catch(Exception ee)
		{}
	}
	
	public int getHttpPort()
	{
		return httpPort ;
	}
	
	public void setHttpPort(int p)
	{
		httpPort = p ;
	}
	
	public int getMySqlPort()
	{
		return mySqlPort ;
	}
	
	public void setMySqlPort(int p)
	{
		mySqlPort = p ;
	}
	
	public String getRootPsw()
	{
		if(rootPsw==null)
			return "" ;
		return rootPsw ;
	}
	
	public void setRootPsw(String p)
	{
		rootPsw = p ;
		rootPswToDB = false;
	}
	
	public boolean isRootPswToDB()
	{
		return rootPswToDB;
	}
	
	/**
	 * ��ϵͳ��������֮����֤root����ʱ��Ҫ�ж��Ƿ�ѱ��������õ����ݿ���
	 * @param brptdb
	 */
	public void setRootPswToDB(boolean brptdb)
	{
		rootPswToDB = brptdb;
	}
	
	public void saveConf() throws Exception
	{
		XmlData.writeToFile(this.toXmlData(), confFile);
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		xd.setParamValue("http_port", httpPort) ;
		xd.setParamValue("mysql_port", mySqlPort) ;
		if(rootPsw!=null)
			xd.setParamValue("root_psw", rootPsw) ;
		
		xd.setParamValue("root_psw_to_db", rootPswToDB);
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		httpPort = xd.getParamValueInt32("http_port", DEF_HTTP_PORT) ;
		mySqlPort = xd.getParamValueInt32("mysql_port", DEF_MYSQL_PORT) ;
		rootPsw = xd.getParamValueStr("root_psw") ;
		rootPswToDB = xd.getParamValueBool("root_psw_to_db", false);
	}
}
