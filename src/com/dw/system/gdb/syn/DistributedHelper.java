package com.dw.system.gdb.syn;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.AppConfig;

/**
 * �ֲ�ʽ����µ�Server Proxy֧����
 * 
 * server ͨ��url�ṩ��proxy��ͨ�ţ��Ǳ�������
 * 
 * 1������ģʽ1�����ݿ�����־ͬ����proxy��server���Ͷ�Ӧ����־��Ϣ
 * 
 * @author Jason Zhu
 */
public class DistributedHelper
{
	/**
	 * ����״̬�µĴ�����Ϣ
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
				{//ɨ��ÿ��ģʽ1�ı��ж������Ƿ�����־������У�ȡ���ϵ�һ�����д���
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
	 * �ڴ�������ģʽ�£�����ͬ��ģʽ1�߳�
	 * ���̲߳��ϵ�ɨ��ģʽ1����־��������������ݣ������̽��з���
	 * ���ݷ������ķ��������������ͳɹ���ɾ�����ض�Ӧ����־��Ϣ��
	 * 
	 * ÿ�η���ʱ������Я����־��Ϣ֮�⣬���б��صĴ�����Ϣ
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
	 * �������ݿ�����ʣ���ʼ��Proxy��Ӧ�ı�
	 * 
	 * �����������ڵ�һ��Proxy��Ҫ���г�ʼ������Ӧ��mode1�����ݱ���Ҫ��Server��
	 * ��������Լ������ݣ��Գ�ʼ��������Ҫ�����ݡ�
	 * 
	 * �ڵ��ñ�����ǰ������Ȳ鿴һ������ı�����־��ȷ�����صı��ǿյ�
	 * 
	 * @param tablename
	 */
	public static void P_initProxySynMode1(String tablename)
	{
		AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
		if(pi==null)
			return ;
		//�����������ȶ��Լ��ı����������ݲ���??
		
		//����������֧����id��С���󣬲���Ϊ�˼����������������Ҫʹ�ö���ָ����з���
		//ÿ��ֻ��ȡ���ֵ�����
		
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
								{//ͬ���ɹ�����Ҫ��¼���ظ��µ�ʱ���,�������Ա����ظ�����
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
	 * �ڴ�������ģʽ�£�����ͬ��ģʽ1�߳�
	 * ���̲߳��ϵ�ɨ��ģʽ1����־��������������ݣ������̽��з���
	 * ���ݷ������ķ��������������ͳɹ���ɾ�����ض�Ӧ����־��Ϣ��
	 * 
	 * ÿ�η���ʱ������Я����־��Ϣ֮�⣬���б��صĴ�����Ϣ
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
	 * ��Server��ʹ�õģ��������������Ϣ
	 * ���������ʷ������˳����������־״����������״����
	 * 
	 */
	static HashMap<String,RtProxyInfo> proxyId2RtInfo = new  HashMap<String,RtProxyInfo>() ;
	
}
