package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.AppConfig;
import com.dw.system.logger.*;
import com.dw.system.xmldata.*;


/**
 * �ⲿ�����¼����
 * 
 * Ϊ�ⲿ����ϵͳ�ṩ�����¼��֧��
 * ˼·��
 * 	1���ṩ�û���֤�ӿ�-WebService����ȽϺ���
 *  2���ṩ�Ѿ���¼�б�
 *  3���ṩ�û��б�
 *  4���ṩ�û���ɫ��ϵ�б�
 * 
 * @author Jason Zhu
 */
public class OuterSSOManager
{
//	RightManager rightMgr = null ;
	static OuterSSOManager ssoMgr = new OuterSSOManager();
	
	static ILogger log = LoggerManager.getLogger(OuterSSOManager.class) ;
	
	public static OuterSSOManager getInstance()
	{
		return ssoMgr ;
	}
	
	/**
	 * ���������ⲿϵͳip����
	 */
	HashSet<String> allOuterSysIps = new HashSet<String>() ;
	
	private OuterSSOManager()
	{
		loadConf();
	}
	
	
	private void loadConf()
	{
		try
		{
			String fn = AppConfig.getDataDirBase()+"outer_sso.xml" ;
			XmlData xd = XmlData.readFromFile(fn) ;
			if(xd==null)
				return ;
			
			String[] ips = xd.getParamValuesStr("allow_ips") ;
			if(ips!=null)
			{
				for(String ip:ips)
				{
					allOuterSysIps.add(ip) ;
				}
			}	
		}
		catch(Exception ee)
		{
			if(log.isErrorEnabled())
				log.error(ee);
			return ;
		}
	}
	
	
	private void saveConf()
	{
		try
		{
			String fn = AppConfig.getDataDirBase()+"outer_sso.xml" ;
			
			File f = new File(fn) ;
			if(!f.getParentFile().exists())
				f.getParentFile().mkdirs() ;
			
			ArrayList<String> ips = new ArrayList<String>();
			ips.addAll(allOuterSysIps) ;
			
			XmlData xd = new XmlData() ;
			xd.setParamValues("allow_ips", ips);
			
			XmlData.writeToFile(xd, fn) ;
		}
		catch(Exception ee)
		{
			if(log.isErrorEnabled())
				log.error(ee);
			return ;
		}
	}
	
	/**
	 * ������������ip
	 * @return
	 */
	public String[] getAllAllowedIp()
	{
		String[] rets = new String[allOuterSysIps.size()] ;
		allOuterSysIps.toArray(rets) ;
		return rets ;
	}
	
	/**
	 * �ж�ip�Ƿ�����ʹ�ñ�ϵͳ����֤
	 * ͨ������֤�����Ա����ⲿ�ķ��ʹ���
	 * @param ip
	 * @return
	 */
	public boolean isIpAllowed(String ip)
	{
		return allOuterSysIps.contains(ip) ;
	}
	
	
	public void setAllowedIp(String ip)
	{
		if(allOuterSysIps.contains(ip))
			return ;
		
		allOuterSysIps.add(ip) ;
		
		saveConf() ;
	}
	
	
	public void unsetAllowedIp(String ip)
	{
		if(!allOuterSysIps.contains(ip))
			return ;
		
		allOuterSysIps.remove(ip);
		
		saveConf() ;
	}
}
