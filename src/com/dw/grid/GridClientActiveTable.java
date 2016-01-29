package com.dw.grid;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;

public class GridClientActiveTable
{
	private static long TIMEOUT = 60000 ;
	
	public static class ClientItem
	{
		/**
		 * Client 标记
		 */
		long clientId = -1 ;
		
		/**
		 * 终端标题
		 */
		String title = null ;
		/**
		 * 最近活动时间
		 */
		long lastActiveMS= -1 ;
		
		/**
		 * 位置
		 */
		GpsPos lastGpsPos = null ;
		
		/**
		 * 最后一次gps位置获取时间
		 */
		long lastGpsPosFoundTime = -1 ;
		
		public ClientItem(long clientid,String title,long last_act_ms,GpsPos lgp)
		{
			this.clientId = clientid ;
			this.title = title ;
			this.lastActiveMS = last_act_ms ;
			if(lgp!=null)
			{
				lastGpsPos = lgp ;
				lastGpsPosFoundTime = System.currentTimeMillis() ;
			}
		}
		
		
		public long getClientId()
		{
			return clientId ;
		}
		
		public String getClientTitle()
		{
			return title ;
		}
		
		public long getLastActiveTime()
		{
			return lastActiveMS ;
		}
		
		public GpsPos getLastGpsPos()
		{
			return lastGpsPos ;
		}
		
		/**
		 * 
		 * @return
		 */
		public long getLastGpsPosFoundTime()
		{
			return lastGpsPosFoundTime ;
		}
		
		public boolean isTimeout()
		{
			return System.currentTimeMillis() - lastActiveMS > TIMEOUT;
		}
	}
	
	
	private Hashtable<Long,ClientItem> id2ci = new Hashtable<Long,ClientItem>() ;
	
	GridClientActiveTable()
	{}
	
	
	public void setActiveClient(long clientid,GpsPos gp)
		throws Exception
	{
		GridClientItem gci = GridClientManager.getInstance().getClientById(clientid) ;
		String title ="noname" ;
		if(gci!=null)
		{
			title = gci.getTitle() ;
		}
		ClientItem ci = new ClientItem(clientid,title,System.currentTimeMillis(),gp) ;
		id2ci.put(clientid, ci) ;
	}
	
	
	public void setActiveClient(long clientid,String title,GpsPos gp)
		throws Exception
	{
			if(Convert.isNullOrEmpty(title))
				title = "noname" ;
		
		ClientItem ci = new ClientItem(clientid,title,System.currentTimeMillis(),gp) ;
		id2ci.put(clientid, ci) ;
	}
	
	//int c = 0 ;

	public List<ClientItem> listActiveClientItems()
	{
		ArrayList<ClientItem> rets = new ArrayList<ClientItem>();
		
		synchronized(id2ci)
		{
			for(ClientItem ci:id2ci.values())
			{
				if(ci.isTimeout())
					continue ;
				rets.add(ci) ;
			}
		}
		
		return rets ;
	}
}
