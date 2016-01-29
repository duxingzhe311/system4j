package com.dw.system.gdb.syn;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.AppConfig;

/**
 * 分布式情况下的Server Proxy支持类
 * 
 * server 通过url提供和proxy的通信，是被动访问
 * 
 * 1，对于模式1的数据库表的日志同步，proxy向server发送对应的日志信息
 * 
 * @author Jason Zhu
 */
public class DistributedHelper
{
	/**
	 * 运行状态下的代理信息
	 * 
	 * @author Jason Zhu
	 *
	 */
	public static class RtProxyInfo
	{
		
	}
	
	static Thread thSynMode1 = null ;
	static Thread thSynMode2 = null ;
	
	public static String getProxyId()
	{
		AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
		if(pi==null)
			return null;
		
		return pi.proxyId ;
	}
	
	private static String getServerCmdUrl()
	{
		AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
		if(pi==null)
			return null;
		return "http://"+pi.serverAddr+"/system/util/distributed_syn.jsp" ;
	}
	
	static Runnable synRnnerMode1 = new Runnable(){

		public void run()
		{
			AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
			if(pi==null)
				return ;
			
			System.out.println(">>>>gdb distributed syn mode1 started!!") ;
			
			String url = getServerCmdUrl();
			try
			{
				while(thSynMode1!=null)
				{//扫描每个模式1的表，判断里面是否有日志，如果有，取最老的一个进行处理
					GDBLogManager logmgr = GDBLogManager.getInstance();
					//logmgr.getAllMode1Tables() ;
					//ArrayList<GDBLogTable> lts =  .getAllLogTables() ;
					for(String tn:logmgr.getAllMode1Tables())
					{
						GDBLogTable lt = logmgr.getLogTable(tn) ;
						GDBLogItem li = null ;
						String get_log_err = null ;
						int logn = -1 ;
						try
						{
							logn = lt.getLogCount() ;
							//lt.
							li = lt.getOldestLogItem() ;
						}
						catch(Exception ee)
						{
							//mi.
							get_log_err = ee.getMessage();
						}
					
						PSCSynMode1 m1 = new PSCSynMode1(logn,li,get_log_err) ;
						
						try
						{
							m1.Proxy_sendToServer(pi.proxyId,url) ;
						}
						catch(Exception ee)
						{}
					}
					
					Thread.sleep(10000) ;
				}
			}
			catch(Exception ee)
			{
				
			}
			finally
			{
				thSynMode1 = null ;
			}
		}
		
	};
	
	/**
	 * 在代理运行模式下，启动同步模式1线程
	 * 本线程不断的扫描模式1的日志表，如果发现有内容，则立刻进行发送
	 * 根据服务器的反馈情况，如果发送成功则删除本地对应的日志信息。
	 * 
	 * 每次发送时，除了携带日志信息之外，还有本地的代理信息
	 * @return
	 */
	public synchronized static void startProxySynMode1()
	{
		if(!AppConfig.isRunAsDistributedProxy())
			return ;
		
		if(thSynMode1!=null)
			return ;
		
		thSynMode1 = new Thread(synRnnerMode1,"proxy_syn_mode1") ;
		thSynMode1.start() ;
	}
	
	public static void stopProxySynMode1()
	{
		if(thSynMode1==null)
			return ;
		
		thSynMode1.interrupt() ;
		thSynMode1 = null;
	}
	
	public static boolean isProxySynMode1Running()
	{
		return thSynMode1!=null ;
	}
	
	/**
	 * 根据数据库表名词，初始化Proxy对应的表
	 * 
	 * 本方法适用于当一个Proxy需要进行初始化，对应的mode1的数据表需要从Server端
	 * 获得属于自己的数据，以初始化自身需要的内容。
	 * 
	 * 在调用本方法前，最好先查看一下自身的本地日志，确保本地的表是空的
	 * 
	 * @param tablename
	 */
	public static void P_initProxySynMode1(String tablename)
	{
		AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
		if(pi==null)
			return ;
		//本方法，首先对自己的表进行清除内容操作??
		
		//由于自增长支付串id从小到大，并且为了减轻服务器负担，需要使用多条指令进行发送
		//每次只读取部分的内容
		
		GDBLogTable lt = GDBLogManager.getInstance().getLogTable(tablename) ;
		if(lt==null)
			return ;
		
		//
		PSCSynMode1Init cmd = new PSCSynMode1Init(lt,20) ;
		ProxyServerCmd.ProxySentRes r = null ;
		do
		{
			r = cmd.Proxy_sendToServer(pi.proxyId, getServerCmdUrl());
		}
		while(r==ProxyServerCmd.ProxySentRes.succ_repeat);
	}
	
	
	static Runnable synRnnerMode2 = new Runnable(){

		public void run()
		{
			AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
			if(pi==null)
				return ;
			
			System.out.println(">>>>gdb distributed syn mode2 started!!") ;
			
			String url = getServerCmdUrl();
			try
			{
				while(thSynMode2!=null)
				{//
					GDBLogManager logmgr = GDBLogManager.getInstance() ;
					for(String tn:logmgr.getAllMode2Tables())
					{
						String get_log_err = null ;
						try
						{
							PSCSynMode2Check m2ck = new PSCSynMode2Check(tn) ;
							//System.out.println("before check send to server--------") ;
							m2ck.Proxy_sendToServer(getProxyId(), getServerCmdUrl()) ;
					//System.out.println("after check send to server--------") ;
							GDBLogItem li = null ;
							
							int logn = -1 ;
						
							if(!m2ck.isCheckSucc())
								continue ;
							
							long server_ts = m2ck.getServerTimestamp() ;
							
							long local_ts = PSCSynMode2.Proxy_readTimestamp(tn) ;
							
							//System.out.println("after check send to server--------sts="+server_ts+" lts="+local_ts) ;
							if(server_ts==local_ts)
								continue ;
							
							PSCSynMode2 m2 = new PSCSynMode2(tn,20) ;
							do
							{
								//System.out.println("before syn send to server--------pk="+m2.lastMaxPk) ;
								ProxyServerCmd.ProxySentRes r = m2.Proxy_sendToServer(getProxyId(), getServerCmdUrl()) ;
								//System.out.println("after syn send to server--------pk="+m2.lastMaxPk) ;
								if(r==ProxyServerCmd.ProxySentRes.succ_repeat)
								{
									continue ;
								}
								else if(r==ProxyServerCmd.ProxySentRes.succ_normal)
								{//同步成功，需要记录本地更新的时间戳,这样可以避免重复更新
									//System.out.println("Proxy_saveTimestamp sts="+server_ts) ;
									PSCSynMode2.Proxy_saveTimestamp(tn, server_ts) ;
									break ;
								}
								else
								{
									m2=null;
									break ;
								}
							}
							while(m2!=null);
						}
						catch(Exception ee)
						{
							ee.printStackTrace();
							get_log_err = ee.getMessage();
						}
					}//end of for
					
					try
					{
						Thread.sleep(10000) ;
					}
					catch(Exception es)
					{}
				}//end of while
				
				//System.out.println("end of while---------");
			}
			finally
			{
				thSynMode2 = null ;
			}
		}
		
	};
	
	/**
	 * 在代理运行模式下，启动同步模式1线程
	 * 本线程不断的扫描模式1的日志表，如果发现有内容，则立刻进行发送
	 * 根据服务器的反馈情况，如果发送成功则删除本地对应的日志信息。
	 * 
	 * 每次发送时，除了携带日志信息之外，还有本地的代理信息
	 * @return
	 */
	public synchronized static void startProxySynMode2()
	{
		if(!AppConfig.isRunAsDistributedProxy())
			return ;
		
		if(thSynMode2!=null)
			return ;
		
		thSynMode2 = new Thread(synRnnerMode2,"proxy_syn_mode2") ;
		thSynMode2.start() ;
	}
	
	public static void stopProxySynMode2()
	{
		if(thSynMode2==null)
			return ;
		
		thSynMode2.interrupt() ;
		thSynMode2 = null;
	}
	
	public static boolean isProxySynMode2Running()
	{
		return thSynMode2!=null ;
	}
	
	/**
	 * 在Server端使用的，代理端运行中信息
	 * 比如代理访问服务器端出错，代理端日志状况，和运行状况等
	 * 
	 */
	static HashMap<String,RtProxyInfo> proxyId2RtInfo = new  HashMap<String,RtProxyInfo>() ;
	
}
