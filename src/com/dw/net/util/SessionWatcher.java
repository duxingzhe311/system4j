package com.dw.net.util;

import java.net.*;
import java.util.*;

/**
 * ����֧�ֻỰ�۲��֧����
 * ���磺
 * 	�ڲ����ʼ�������������ͨ������۲��ն�ʹ�õ�״̬
 * 
 *  �����˼�ع���
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
		 * �ж��Ƿ�����
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
	 * ����һ�����ƻ�ȡ��Ӧ�ù۲���
	 * �磬POP3�۲�����
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
	 * ���ĳһ��ip�Ƿ��Ǳ����Ƶ�ip������ǣ���ᱻ��ֹ����
	 * @return
	 */
	public static boolean checkLimitIP(String ip)
	{
		return true;
	}
}
