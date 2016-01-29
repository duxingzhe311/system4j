package com.dw.system;

import java.io.File;
import java.util.*;

import com.dw.system.logger.*;
import com.dw.system.xmldata.*;

public class DomainManager
{
	/**
	 * 域管理员角色名称
	 */
	public final static String ROLE_DOMAIN_MGR = "domain_mgr" ;
	
	static Object locker = new Object() ;
	static DomainManager instance = null ;
	
	static ILogger log = LoggerManager.getLogger(DomainManager.class) ;
	
	public static DomainManager getInstance()
	{
		if(instance!=null)
			return instance ;
		
		synchronized(locker)
		{
			if(instance!=null)
				return instance ;
			
			instance = new DomainManager() ;
			
			return instance ;
		}
	}
	
	/**
	 * 直接根据配置文件获得对象-可以用来在安装系统的时候进行设置
	 * @param conff
	 * @return
	 */
	public static DomainManager getInsByConfFile(File conff)
	{
		return new DomainManager(conff) ;
	}
	
	File confFile = null ;
	
	int maxId = -1;
	
	DomainItem defaultDomain = null ;
	
	HashMap<Integer,DomainItem> id2domain = new HashMap<Integer,DomainItem>() ;
	
	private DomainManager()
	{
		confFile = new File(AppConfig.getDataDirBase()+"sys_domains.xml") ;
		loadConf();
	}
	
	private DomainManager(File conff)
	{
		confFile = conff;
		loadConf();
	}
	
	
	private void loadConf()
	{
		try
		{
			//String fn = AppConfig.getDataDirBase()+"sys_domains.xml" ;
			XmlData xd = XmlData.readFromFile(confFile) ;
			if(xd==null)
				return ;
			
			maxId = xd.getParamValueInt32("max_id", -1) ;
			
			List<XmlData> xds = xd.getSubDataArray("domains") ;
			if(xds!=null)
			{
				for(XmlData xd0:xds)
				{
					DomainItem di = new DomainItem();
					di.fromXmlData(xd0) ;
					
					if(di.id<0)
						continue ;
					
					if(di.id==0)
					{
						defaultDomain = di;
					}
					
					id2domain.put(di.id, di);
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
	
	
	public void saveConf()
	{
		try
		{
			//String fn = AppConfig.getDataDirBase()+"sys_domains.xml" ;
			
			//File f = new File(fn) ;
			if(!confFile.getParentFile().exists())
				confFile.getParentFile().mkdirs() ;
			
			XmlData xd = new XmlData() ;
			xd.setParamValue("max_id", maxId);
			List<XmlData> tmpxds = xd.getOrCreateSubDataArray("domains") ;
			
			for(DomainItem di:id2domain.values())
			{
				tmpxds.add(di.toXmlData()) ;
			}
			
			XmlData.writeToFile(xd, confFile) ;
		}
		catch(Exception ee)
		{
			if(log.isErrorEnabled())
				log.error(ee);
			return ;
		}
	}
	
	public DomainItem[] getAllDomainItems()
	{
		DomainItem[] rets = new DomainItem[id2domain.size()] ;
		id2domain.values().toArray(rets) ;
		Arrays.sort(rets);
		return rets ;
	}
	
	
	public DomainItem getDomainItemDefault()
	{
		return defaultDomain ;
	}
	
	public DomainItem getDomainItem(int id)
	{
		if(id==0)
			return defaultDomain ;
		
		return id2domain.get(id) ;
	}
	
	
	public DomainItem getDomainItem(String domain)
	{
		for(DomainItem di:id2domain.values())
		{
			if(di.domain.equalsIgnoreCase(domain))
				return di ;
		}
		
		return null ;
	}
	
	
	/**
	 * 根据输入的一个域名判断内部定义的覆盖此域名的设置域名
	 * 如：输入 www.xx.com 会被 xx.com 匹配
	 * @param matchdomain
	 * @return
	 */
	public DomainItem findMatchDomain(String matchdomain)
	{
		if(matchdomain==null)
			return null ;
		
		matchdomain = matchdomain.trim().toLowerCase() ;
		for(DomainItem di:id2domain.values())
		{
			if(matchdomain.endsWith(di.domain.toLowerCase()))
				return di ;
		}
		
		return null ;
	}
	
	
	public int getDomainId(String domain)
	{
		DomainItem di = getDomainItem(domain) ;
		if(di==null)
			return -1 ;
		
		return di.getId() ;
	}
	
	public void setDomain(int id,String d,int max_user)
	{
		setDomain(-1,id,d,max_user);
	}
	
	public void setDomain(int id,String d)
	{
		DomainItem di = id2domain.get(id) ;
		if(di==null)
			return ;
		
		setDomain(-1,id,d,di.getMaxUserNum());
	}
	
	public void setDomainMaxUser(int id,int max_user)
	{
		DomainItem di = id2domain.get(id) ;
		if(di==null)
			return ;
		
		setDomain(-1,id,di.getDomain(),max_user);
	}
	
	public void setDomain(int oldid,int id,String d)
	{
		DomainItem odi = id2domain.get(oldid) ;
		int num = -1;
		if(odi!=null)
			num = odi.getMaxUserNum() ;
		setDomain(oldid,id,d,num) ;
	}
	
	public void setDomain(int oldid,int id,String d,int max_user)
	{
		if(id<0)
		{
			id = maxId + 1 ;
			maxId = id ;
		}
		else
		{
			if(id>maxId)
				maxId = id ;
		}
		
		
		if(oldid>=0)
			id2domain.remove(oldid) ;
		
		DomainItem di = new DomainItem(id,d,max_user) ;
		id2domain.put(id,di) ;
		
		saveConf() ;
		
		if(id==0)
			defaultDomain = di ;
	}
	
	
	public void unsetDomain(int id)
	{
		DomainItem di = getDomainItem(id) ;
		if(di==null)
			return ;
		
		id2domain.remove(id);
		
		saveConf() ;
		
		if(id==0)
			defaultDomain = null ;
	}
}
