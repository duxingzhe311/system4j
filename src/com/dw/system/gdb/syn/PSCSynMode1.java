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
 * ֧��ģʽ1��Proxy Server����
 * 
 * Proxy������־��Server��server���±��ض�Ӧ�ı�����Ӧ����
 * Proxy���ֳɹ���ɾ��������־
 * 
 * ע��<br>
 * Ϊ�˱�֤�ɿ������ڷ������˸��³ɹ����ֿ�������Ӧ����������������⡣Proxy�˲���
 * ��֤֪�����������Ѿ��ɹ������ԣ���ÿ����Ϣ�У�����Ψһid-��ProxyId+��־Ψһid
 * 
 * Server��ʹ��һ���ڴ���ʱ����¼��ǰ�Ѿ����³ɹ���id�����Proxy���ظ����ͣ�������
 * ���سɹ���Ϣ
 * 
 * @author Jason Zhu
 */
public class PSCSynMode1 extends ProxyServerCmd
{
	public static class ProxyTableInfo
	{
		String tableName = null ;
		//proxy��ʣ�����־��
		int proxyLeftLogNum = -1 ;
		
		String proxyErr = null;
		
		//������յ���ͬ����־
		LinkedList<GDBLogItem> lastLogItems = new LinkedList<GDBLogItem>() ;
		
		//���ͬ��ʱ��-Server��ʱ��
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
	 * Server����ʱ��������ɹ�������id��Ϣ
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
	 * ���й����У�����id��Ӧ��ʵʱ��Ϣ
	 */
	static HashMap<String,ProxyMode1Info> RT_proxyId2Info = new HashMap<String,ProxyMode1Info>() ;
	
	/**
	 * ���й����У�������еĴ���Mode1��״̬��Ϣ
	 * @return
	 */
	public static List<ProxyMode1Info> RT_getAllProxyMode1Infos()
	{
		ArrayList<ProxyMode1Info> rets = new ArrayList<ProxyMode1Info>() ;
		rets.addAll(RT_proxyId2Info.values()) ;
		return rets ;
	}
	/**
	 * ��������ͬ�����´���
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
	 * �Ĺ��췽��Proxyʹ�ã��������췢�Ͷ���
	 * @param proxyid
	 * @param li
	 */
	public PSCSynMode1(int lognum,GDBLogItem li,String proxy_err)
	{
		logNum = lognum ;//proxy�˵���־����
		proxyErr = proxy_err ;//proxy�˵Ĵ�����Ϣ
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
				//��������־ɾ��
				GDBLogTable lt = GDBLogManager.getInstance().getLogTable(logItem.getTableName()) ;
				lt.delLogByDT(logItem.getLogDT(), logItem.getLogInDTCount()) ;
				
				//ɾ����һ�δ���
				Proxy_table2Error.remove(logItem.getTableName()) ;
			}
			return ProxySentRes.succ_normal ;
		}
		else
		{
			String errmsg = resp_xd.getParamValueStr("error_msg") ;
			//��¼����
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
		//��¼����
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
			//���±���
			doUpdateByLogInServer(conn,proxyid,li) ;
		}
		catch(Exception e)
		{
			boolean b_insert_dup_pk = false;//�Ƿ���insert������ͻ
			if(li.isInsertSql())
			{//�����Insert��䣬���п����ظ����ж��Ƿ��������ظ�
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
			//��Ϊ��һЩ�߽�����£��п���Server��־���³ɹ�֮��û�м�ʱͬʱProxy
			//ͬʱServer�������������⵼��
			
			XmlData tmpxd = new XmlData() ;
			tmpxd.setParamValue("syn_res", false) ;
			tmpxd.setParamValue("is_insert_dup_pk", b_insert_dup_pk) ;
			tmpxd.setParamValue("error_msg", e.getMessage()) ;
			
			//��¼����
			Server_table2Error.put(li.getTableName(), e.getMessage()) ;
			
			return tmpxd ;
		}
		//���³ɹ�-��¼ĳһ������Ķ�Ӧ����������id���Է�ֹProxy�ظ�����ͬһ��������־
		Server_ProxyTable2Id.put(key,val) ;
		
		//�������
		Server_table2Error.remove(li.getTableName()) ;
		return RESP_SUCC_XD;
	}
	/**
	 * ������־���󣬶Ա������ݿ���и��²���
	 * ���������ڲ�֧�ַ�������粻����ʹ��
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
