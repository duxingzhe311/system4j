package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.AppConfig;
import com.dw.system.logger.*;
import com.dw.system.xmldata.*;


/**
 * 外部单点登录管理
 * 
 * 为外部其他系统提供单点登录的支持
 * 思路：
 * 	1，提供用户验证接口-WebService好像比较合适
 *  2，提供已经登录列表
 *  3，提供用户列表
 *  4，提供用户角色关系列表
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
	 * 存放允许的外部系统ip集合
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
	 * 获得所有允许的ip
	 * @return
	 */
	public String[] getAllAllowedIp()
	{
		String[] rets = new String[allOuterSysIps.size()] ;
		allOuterSysIps.toArray(rets) ;
		return rets ;
	}
	
	/**
	 * 判断ip是否允许使用本系统的验证
	 * 通过此验证，可以避免外部的访问攻击
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
