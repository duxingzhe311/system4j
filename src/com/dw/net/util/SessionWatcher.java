package com.dw.net.util;

import java.net.*;
import java.util.*;

/**
 * 用来支持会话观察的支持类
 * 比如：
 * 	内部的邮件服务器，可以通过该类观察终端使用的状态
 * 
 *  增加了监控功能
 * @author Jason Zhu 
 */
public class SessionWatcher
{
	public static class SessionInfo
	{
		Socket sessSock = null ;
		long startTS = -1 ;
		String extInfo = null;
		
		SessionInfo(Socket socket,String extinfo)
		{
			sessSock = socket ;
			startTS = System.currentTimeMillis() ;
			
			extInfo=extinfo;
		}
		
		/**
		 * 判断是否连接
		 * @return
		 */
		public boolean isConnected()
		{
			return sessSock.isConnected();
		}
		
		public String toDescString()
		{
			if(!isConnected())
				return "Not Connected" ;
			
			StringBuilder sb = new StringBuilder() ;
			sb.append("  Live[").append(System.currentTimeMillis()-startTS).append("]ms");
			int localport = sessSock.getLocalPort() ;
			if(localport>0)
			{
				sb.append(" LocalPort[").append(localport).append("]");
			}
			InetAddress raddr = sessSock.getInetAddress() ;
			if(raddr!=null)
			{
				int rport = sessSock.getPort() ;
				sb.append(" Remote[")
					.append(raddr.getHostName()).append("-")
					.append(raddr.getHostAddress()).append(":").append(rport)
					.append("]") ;
			}
			
			if(extInfo!=null)
				sb.append(extInfo);
			
			return sb.toString() ;
		}
	}
	
	static Object locker = new Object() ;
	static HashMap<String,SessionWatcher> name2watch = new HashMap<String,SessionWatcher>() ;
	
	/**
	 * 根据一个名称获取的应得观察器
	 * 如，POP3观察器等
	 * @param name
	 * @return
	 */
	public static SessionWatcher getOrCreateWatcher(String name)
	{
		SessionWatcher sw = name2watch.get(name) ;
		if(sw!=null)
			return sw ;
		
		synchronized(locker)
		{
			sw = name2watch.get(name) ;
			if(sw!=null)
				return sw ;
			
			sw = new SessionWatcher(name) ;
			name2watch.put(name, sw) ;
			return sw ;
		}
	}
	
	private String name = null ;
	
	private HashMap<Socket,SessionInfo> sock2Info = new HashMap<Socket,SessionInfo>() ;
	
	private SessionWatcher(String name)
	{
		this.name = name ;
	}
	
	public String getName()
	{
		return name ;
	}
	
	public void startWatch(Socket sock,String extinfo)
	{
		if(sock==null)
			return ;
		
		SessionInfo si = new SessionInfo(sock,extinfo) ;
		sock2Info.put(sock, si) ;
	}
	
	
	public void stopWatch(Socket sock)
	{
		if(sock==null)
			return ;
		
		sock2Info.remove(sock) ;
	}
	
	
	public SessionInfo[] getAllSessionInfo()
	{
		SessionInfo[] rets = new SessionInfo[sock2Info.size()] ;
		sock2Info.values().toArray(rets) ;
		return rets ;
	}
	
	/**
	 * 检测某一个ip是否是被限制的ip，如果是，则会被阻止处理
	 * @return
	 */
	public static boolean checkLimitIP(String ip)
	{
		return true;
	}
}
