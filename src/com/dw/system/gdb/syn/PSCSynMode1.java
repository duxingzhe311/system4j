package com.dw.system.gdb.syn;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.gdb.ConnPoolMgr;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GdbException;
import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.conf.autofit.JavaTableInfo;
import com.dw.system.gdb.connpool.GDBConn;
import com.dw.system.gdb.connpool.IConnPool;
import com.dw.system.xmldata.*;

/**
 * 支持模式1的Proxy Server端类
 * 
 * Proxy发送日志到Server，server更新本地对应的表，并响应返回
 * Proxy发现成功后，删除本地日志
 * 
 * 注：<br>
 * 为了保证可靠，由于服务器端更新成功后，又可以在响应过程中网络出现问题。Proxy端不能
 * 保证知道服务器端已经成功。所以，在每个消息中，定义唯一id-如ProxyId+日志唯一id
 * 
 * Server端使用一个内存临时表，记录当前已经更新成功的id。如果Proxy端重复发送，则立刻
 * 返回成功信息
 * 
 * @author Jason Zhu
 */
public class PSCSynMode1 extends ProxyServerCmd
{
	public static class ProxyTableInfo
	{
		String tableName = null ;
		//proxy端剩余的日志数
		int proxyLeftLogNum = -1 ;
		
		String proxyErr = null;
		
		//最近接收到的同步日志
		LinkedList<GDBLogItem> lastLogItems = new LinkedList<GDBLogItem>() ;
		
		//最近同步时间-Server端时间
		long lastSynDT = System.currentTimeMillis() ;
		
		transient int cacheLen = 10 ;
		
		ProxyTableInfo(String tablen)
		{
			this.tableName = tablen ;
		}
		
		public String getTableName()
		{
			return tableName ;
		}
		
		public int getProxyLeftLogNum()
		{
			return proxyLeftLogNum ;
		}
		
		public List<GDBLogItem> getLastLogItems()
		{
			ArrayList<GDBLogItem> rets = new ArrayList<GDBLogItem>() ;
			rets.addAll(lastLogItems) ;
			
			return rets ;
		}
		
		void addLogItem(GDBLogItem li)
		{
			lastLogItems.addLast(li) ;
			while(lastLogItems.size()>cacheLen)
			{
				lastLogItems.removeFirst() ;
			}
		}
		
		public long getLastSynDT()
		{
			return this.lastSynDT ;
		}
	}
	
	public static class ProxyMode1Info
	{
		String proxyId = null ;
		String proxyTitle = null ;
		
		HashMap<String,ProxyTableInfo> table2info = new HashMap<String,ProxyTableInfo>() ;
		
		ProxyMode1Info(String proxyid)
		{
			proxyId = proxyid ;
		}
		
		public String getProxyId()
		{
			return proxyId ;
		}
		
		void setRTInfo(String tablename,long syndt,int lognum,GDBLogItem curlog,String proxyerr)
		{
			String tn = tablename.toLowerCase() ;
			ProxyTableInfo pti = table2info.get(tn) ;
			if(pti==null)
			{
				pti = new ProxyTableInfo(tn) ;
				table2info.put(tn,  pti) ;
			}
			
			pti.lastSynDT = syndt ;
			pti.proxyLeftLogNum = lognum ;
			pti.proxyErr = proxyerr;
			if(curlog!=null)
				pti.addLogItem(curlog) ;
		}
	}
	/**
	 * Server端临时保存最近成功的日子id信息
	 */
	static HashMap<String,String> Server_ProxyTable2Id = new HashMap<String,String>() ;
	
	static XmlData RESP_SUCC_XD = new XmlData() ;
	static
	{
		RESP_SUCC_XD.setParamValue("syn_res", true) ;
	}
	
	static HashMap<String,String> Proxy_table2Error = new HashMap<String,String>() ;
	static HashMap<String,String> Server_table2Error = new HashMap<String,String>() ;
	
	/**
	 * 运行过程中，代理id对应的实时信息
	 */
	static HashMap<String,ProxyMode1Info> RT_proxyId2Info = new HashMap<String,ProxyMode1Info>() ;
	
	/**
	 * 运行过程中，获得所有的代理Mode1的状态信息
	 * @return
	 */
	public static List<ProxyMode1Info> RT_getAllProxyMode1Infos()
	{
		ArrayList<ProxyMode1Info> rets = new ArrayList<ProxyMode1Info>() ;
		rets.addAll(RT_proxyId2Info.values()) ;
		return rets ;
	}
	/**
	 * 获得最近的同步更新错误
	 * @return
	 */
	public static HashMap<String,String> Proxy_getLastTable2Error()
	{
		HashMap<String,String> rr = new HashMap<String,String>() ;
		rr.putAll(Proxy_table2Error);
		return rr ;
	}
	
	public static HashMap<String,String> Server_getLastTable2Error()
	{
		HashMap<String,String> rr = new HashMap<String,String>() ;
		rr.putAll(Server_table2Error);
		return rr ;
	}
	
	int logNum = -1 ;
	String proxyErr = null ;
	//String proxyId = null ;
	//String tableName = null ;
	GDBLogItem logItem = null ;
	
	public PSCSynMode1()
	{}
	
	/**
	 * 改构造方法Proxy使用，用来构造发送对象
	 * @param proxyid
	 * @param li
	 */
	public PSCSynMode1(int lognum,GDBLogItem li,String proxy_err)
	{
		logNum = lognum ;//proxy端的日志总数
		proxyErr = proxy_err ;//proxy端的错误信息
		//proxyId = proxyid ;
		//tableName = tablename ;
		logItem = li ;
	}
	
	@Override
	public String getCmdName()
	{
		return "syn_mode1";
	}

	@Override
	protected XmlData Proxy_getMsgToBeSent()
	{
		XmlData xd = new XmlData() ;
		
		//
		xd.setParamValue("log_num", logNum) ;
		if(Convert.isNotNullEmpty(proxyErr))
			xd.setParamValue("error", proxyErr) ;
		
		if(logItem!=null)
		{
			xd.setSubDataSingle("log_item", logItem.toXmlData());
		}
		
		return xd ;
	}

	@Override
	protected ProxySentRes Proxy_onResponseSucc(XmlData resp_xd) throws Exception
	{
		
				
		boolean res = resp_xd.getParamValueBool("syn_res",false) ;
		if(res || resp_xd.getParamValueBool("is_insert_dup_pk", false))
		{
			if(logItem!=null)
			{
				//做本地日志删除
				GDBLogTable lt = GDBLogManager.getInstance().getLogTable(logItem.getTableName()) ;
				lt.delLogByDT(logItem.getLogDT(), logItem.getLogInDTCount()) ;
				
				//删除上一次错误
				Proxy_table2Error.remove(logItem.getTableName()) ;
			}
			return ProxySentRes.succ_normal ;
		}
		else
		{
			String errmsg = resp_xd.getParamValueStr("error_msg") ;
			//记录错误
			Proxy_table2Error.put(logItem.getTableName(), errmsg);
			return ProxySentRes.error_normal;
		}
	}

	@Override
	protected ProxySentRes Proxy_onRequestError(Exception error)
	{
		Proxy_table2Error.put(logItem.getTableName(), error.getMessage());
		return ProxySentRes.error_normal;
	}
	
	@Override
	protected ProxySentRes Proxy_onResponseError(Exception error)
	{
		//记录错误
		Proxy_table2Error.put(logItem.getTableName(), error.getMessage());
		return ProxySentRes.error_normal;
	}

	protected boolean Server_needDBConn()
	{
		return true ;
	}
	
	@Override
	protected XmlData Server_onRequestRecved(GDBConn conn,String proxyid,XmlData xd)
	{
		ProxyMode1Info pmi = RT_proxyId2Info.get(proxyid);
		if(pmi==null)
		{
			pmi = new ProxyMode1Info(proxyid) ;
			RT_proxyId2Info.put(proxyid, pmi) ;
		}
		
		int logn = xd.getParamValueInt32("log_num", -1) ;
		String proxyerr = xd.getParamValueStr("proxyErr") ;
		XmlData logxd = xd.getSubDataSingle("log_item") ;
		GDBLogItem li = null;
		//String proxyid = xd.getParamValueStr("proxyid");
		//XmlData xd0 = xd.getSubDataSingle("msgcont") ;
		if(logxd!=null)
		{
			li = new GDBLogItem() ;
			li.fromXmlData(logxd) ;
		}
		
		
		pmi.setRTInfo(proxyid, System.currentTimeMillis(), logn, logItem,proxyerr);
		
		if(li!=null)
			return proceLog(conn,proxyid,li);
		else
			return RESP_SUCC_XD;
	}

	private XmlData proceLog(GDBConn conn,String proxyid,GDBLogItem li)
	{
		String key = proxyid+"_"+li.getTableName() ;
		String val = li.getLogDT()+"_"+li.getLogInDTCount();
		
		String vv = Server_ProxyTable2Id.get(key) ;
		
		if(val.equals(vv))
		{//
			return RESP_SUCC_XD;
		}
		
		try
		{
			//更新本地
			doUpdateByLogInServer(conn,proxyid,li) ;
		}
		catch(Exception e)
		{
			boolean b_insert_dup_pk = false;//是否是insert主键冲突
			if(li.isInsertSql())
			{//如果是Insert语句，则有可能重复，判断是否是主键重复
				String tn = li.getTableName() ;
				Class xormc = GDBLogManager.getInstance().getXORMClassByTableName(tn) ;//lt.getXormClass() ;
				Object pkv = li.getLogParam().get(1) ;
				
				if(pkv!=null&&xormc!=null)
				{
					try
					{
						b_insert_dup_pk = GDB.getInstance().getXORMObjByPkId(xormc, pkv)!=null ;
					}
					catch(Exception ee)
					{}
				}
				//XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
				//b_insert_dup_pk
			}
			//因为在一些边界情况下，有可能Server日志更新成功之后，没有及时同时Proxy
			//同时Server被重新启动，这导致
			
			XmlData tmpxd = new XmlData() ;
			tmpxd.setParamValue("syn_res", false) ;
			tmpxd.setParamValue("is_insert_dup_pk", b_insert_dup_pk) ;
			tmpxd.setParamValue("error_msg", e.getMessage()) ;
			
			//记录错误
			Server_table2Error.put(li.getTableName(), e.getMessage()) ;
			
			return tmpxd ;
		}
		//更新成功-记录某一个代理的对应表的最近更新id，以防止Proxy重复发送同一个更新日志
		Server_ProxyTable2Id.put(key,val) ;
		
		//清除错误
		Server_table2Error.remove(li.getTableName()) ;
		return RESP_SUCC_XD;
	}
	/**
	 * 根据日志对象，对本地数据库进行更新操作
	 * 本方法是内部支持方法，外界不允许使用
	 * @param li
	 * @return
	 * @throws GdbException 
	 */
	private void doUpdateByLogInServer(GDBConn conn,String proxyid,GDBLogItem li) throws Exception
	{
		PreparedStatement ps = null ;
		try
		{
			JavaTableInfo jti = li.getJavaTableInfo() ;
			
			String sql = li.getSqlStrInProxy(jti.getPkColumnInfo().getColumnName(), proxyid) ;
			
			ps = conn.prepareStatementNoLog(sql);
			HashMap<Integer,GDBLogParam> id2pms = li.getLogParam() ;
			if(id2pms!=null&&id2pms.size()>0)
			{
				int s = id2pms.size() ;
				for(int i = 1 ; i <= s ; i ++)
				{
					GDBLogParam lp = id2pms.get(i) ;
					lp.setToStatement(ps) ;
				}
			}

			ps.executeUpdate();
		
			ps.close();
			
			ps = null;
		}
		finally
		{
				try
				{
					if(ps!=null)
						ps.close();
				}
				catch (Throwable sqle)
				{
				}
		}
		
	}
}
