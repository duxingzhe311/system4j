package com.dw.system.gdb.syn_client;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import com.dw.system.*;
import com.dw.system.gdb.*;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.xmldata.*;

/**
 * 同步终端管理
 * 
 * @author Jason Zhu
 */
public class SynClientManager
{
	private static Object locker = new Object() ;
	
	private static SynClientManager instance = null ;
	
	/**
	 * 
	 * @return
	 */
	public static SynClientManager getInstance()
	{
		if(instance!=null)
			return instance ;
		
		synchronized(locker)
		{
			if(instance!=null)
				return instance ;
			
			instance = new SynClientManager() ;
			return instance ;
		}
	}
	
	HashMap<String,ISynClientable> table2synt = new HashMap<String,ISynClientable>() ;
	HashMap<String,String> clientTN2ServerTn = new HashMap<String,String>() ;
	
	/**
	 * 表名称到简单同步表对象
	 */
	HashMap<String,ISimpleSynTable> table2SST = new HashMap<String,ISimpleSynTable>();
	
	private SynClientManager()
	{}
	
	/**
	 * 注册
	 * @param module_name
	 * @param ist
	 */
	public void registerSynTable(String module_name,ISynClientable ist)
	{
		String[] colns = ist.getSynClientClientIdColNames() ;
		if(colns==null||colns.length<=0)
			return ;
		
		String server_tn = null ;
		XORMClass xormc = ist.getClass().getAnnotation(XORMClass.class) ;
		if(xormc!=null)
			server_tn = xormc.table_name() ;
		
		for(String coln:colns)
		{
			String clienttn = ist.getSynClientTableName(coln) ;
			if(Convert.isNullOrEmpty(clienttn))
				continue ;
			
			table2synt.put(clienttn, ist);
			if(Convert.isNotNullEmpty(server_tn))
				clientTN2ServerTn.put(clienttn, server_tn);
		}
	}
	
	
	public void registerSimpleSynTable(String module_n,ISimpleSynTable sst)
	{
		table2SST.put(sst.SST_getTableName(), sst) ;
	}
	
	public ISimpleSynTable getSimpleSynTable(String tn)
	{
		return table2SST.get(tn) ;
	}
	
	public ISynClientable getSynTable(String tn)
	{
		return table2synt.get(tn) ;
	}
	
	//public 
	/**
	 * 终端通过，发送上一次更新时间，来向服务器请求
	 * @param tablen
	 * @param clientid
	 * @param last_log_dt
	 * @return
	 */
	private HashMap<String,Long> readChgLog(String client_tablen,String client_col,String clientid,int max_num)
		throws Exception
	{
		Hashtable ht = new Hashtable() ;
		ht.put("$TableName", client_tablen);
		ht.put("@ClientId", client_col+"_"+clientid);
		
		//ht.put("$TableName", tablen);
		//ht.put("$TableName", tablen);
		DBResult dbr = GDB.getInstance().accessDBPage("syn_client.ReadLog", ht,0,max_num) ;
		DataTable dt = dbr.getResultFirstTable() ;
		if(dt==null)
			return null ;
		
		HashMap<String,Long> ret = new HashMap<String,Long>() ;
		for(DataRow dr:dt.getRows())
		{
			String pkid = (String)dr.getValue("PkId") ;
			Long ldt = ((Number)dr.getValue("ChgDT")).longValue() ;
			ret.put(pkid,ldt) ;
		}
		return ret;
	}
	
	public void delSynClientChgLog(String client_tablen,String client_col,String clientid,String pkid,long chg_dt)
		throws Exception
	{
		Hashtable ht = new Hashtable() ;
		ht.put("$TableName", client_tablen);
		ht.put("@ClientId", client_col+"_"+clientid);
		
		ht.put("@PkId", pkid);
		ht.put("@ChgDT", chg_dt);
		GDB.getInstance().accessDB("syn_client.DelLog", ht) ;
	}
	
	/**
	 * 根据输入的最大日志时间，删除本时间及之前的日志
	 * @param tablen
	 * @param clientid
	 * @param max_old_chg_dt
	 * @throws Exception
	 */
	public void delSynClientOldChgLog(String client_tablen,String client_col,String clientid,long max_old_chg_dt)
		throws Exception
	{
		Hashtable ht = new Hashtable() ;
		ht.put("$TableName", client_tablen);
		ht.put("@ClientId", client_col+"_"+clientid);
		
		ht.put("@MaxOldChgDT", max_old_chg_dt);
		GDB.getInstance().accessDB("syn_client.DelLogOld", ht) ;
	}
	
	
	/*
	 * 获得对应终端表的最大日志id
	 * 
	 */
	public long getSynClientMaxLogDT(String client_tablen,String client_col,String clientid)
		throws Exception
	{
		Hashtable ht = new Hashtable() ;
		ht.put("$TableName", client_tablen);
		ht.put("@ClientId", client_col+"_"+clientid);
		
		//ht.put("$TableName", tablen);
		//ht.put("$TableName", tablen);
		DBResult dbr = GDB.getInstance().accessDB("syn_client.MaxLogDT", ht) ;
		Number n = dbr.getResultFirstColOfFirstRowNumber() ;
		if(n==null)
			return -1 ;
		
		return n.longValue() ;
	}
	
	public List<XmlData> onClientReadData(String client_tablen,String clientid,String last_max_pkid,int read_num)
		throws Exception
	{
		ISynClientable ist = table2synt.get(client_tablen) ;
		if(ist==null)
			return null ;
		
		String coln = ist.getSynClientColByTableName(client_tablen) ;
		return ist.getSynClientData(coln,clientid, last_max_pkid, read_num);
	}
	
	/**
	 * 终端增加的数据
	 * 服务器添加成功后，返回此记录的对应值，以便于终端本地做插入操作
	 * 比如：返回值里面必然有id
	 * @param clientid
	 * @param username
	 * @param client_xd
	 * @return 返回新增id号
	 */
	public XmlData onClientAddData(String client_tablen,String clientid,XmlData client_xd)
		throws Exception
	{
		ISynClientable ist = table2synt.get(client_tablen) ;
		if(ist==null)
			return null ;
		
		String coln = ist.getSynClientColByTableName(client_tablen) ;
		return ist.onSynClientAdd(coln,clientid, client_xd) ;
	}
	
	/**
	 * 
	 * @param clientid
	 * @param username
	 * @param update_cols
	 * @param client_xd
	 * @throws Exception
	 */
	public void onClientUpdateData(String client_tablen,String clientid,String pkid,String[] update_cols,XmlData client_xd)
		throws Exception
	{
		ISynClientable ist = table2synt.get(client_tablen) ;
		if(ist==null)
			return  ;
		
		String coln = ist.getSynClientColByTableName(client_tablen) ;
		ist.onSynClientUpdate(coln,clientid,pkid, update_cols, client_xd) ;
	}
	
	/**
	 * 当终端发送删除请求时，服务器做的响应
	 * @param clientid
	 * @param username
	 * @param pk_ids
	 * @throws Exception
	 */
	public void onClientDeleteData(String client_tablen,String clientid,String[] pk_ids)
		throws Exception
	{
		ISynClientable ist = table2synt.get(client_tablen) ;
		if(ist==null)
			return  ;
		
		String coln = ist.getSynClientColByTableName(client_tablen) ;
		ist.onSynClientDelete(coln,clientid, pk_ids) ;
	}
	
	public void SST_synInJsp(String clientid,String msgtype,InputStream req_in,OutputStream resp_out)
		throws Exception
	{
		XmlData xd = XmlData.parseFromStream(req_in,"utf-8") ;
		if(xd==null)
			return ;
		
		String tablen = xd.getParamValueStr("table_name") ;
		if(Convert.isNullOrEmpty(tablen))
			return ;
		
		ISimpleSynTable ist = this.getSimpleSynTable(tablen);
		if(ist==null)
			return ;
		
		if(Convert.isNullOrEmpty(clientid))
			return ;
		//System.out.println(xd.toXmlString()) ;
		
		if(msgtype.equals("sst_read_id_dt"))
		{
			long lastdt = xd.getParamValueInt64("last_dt", -1) ;
			//String lastid = xd.getParamValueStr("last_max_pk") ;
			
			List<SynItem> sis = null;
			if(lastdt>0)
				sis = ist.SST_readIdAndDTAfter(clientid, lastdt);
			else
				sis = ist.SST_readIdAndDTValidAll(clientid) ;
			
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			if(sis!=null)
			{
				XmlData tmpxd = new XmlData() ;
				for(SynItem si:sis)
				{
					tmpxd.setParamValue(si.getPkId(), si.getUpDT()) ;
				}
				retxd.setSubDataSingle("id2dt", tmpxd);
			}
			//System.out.println(retxd.toXmlString());
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("sst_read_items"))
		{
			String[] pkids = xd.getParamValuesStr("pkids") ;
			if(pkids==null||pkids.length<=0)
				return ;
			//String lastid = xd.getParamValueStr("last_max_pk") ;
			List<SynItem> sis = ist.SST_readItemsDetail(clientid, pkids);
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			if(sis!=null)
			{
				List<XmlData> tmpxds = new ArrayList<XmlData>() ;
				for(SynItem si:sis)
				{
					tmpxds.add(si.toXmlData());
				}
				retxd.setSubDataArray("items", tmpxds);
			}
			//System.out.println(retxd.toXmlString());
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("sst_add"))
		{
			String add_t = xd.getParamValueStr("add_type") ;
			XmlData tmpxd = xd.getSubDataSingle("add_xd");
			SynItem si = ist.SST_onClientAdd(clientid, add_t, tmpxd);
			if(si!=null)
			{
				XmlData retxd = new XmlData() ;
				retxd.setParamValue("res", "succ") ;
				retxd.setSubDataSingle("item", si.toXmlData()) ;
				retxd.writeOutCompact(resp_out,"utf-8") ;
			}
			return ;
		}
		else if(msgtype.equals("sst_update"))
		{
			String pkid = xd.getParamValueStr("pkid") ;
			String update_t = xd.getParamValueStr("update_type") ;
			XmlData tmpxd = xd.getSubDataSingle("update_xd");
			SynItem si = ist.SST_onClientUpdate(clientid, update_t,pkid, tmpxd);
			
			if(si==null)
				return;
			
			
			XmlData retxd = new XmlData() ;
			if(si.updateRes==SynItem.UpdateRes.update)
			{
				retxd.setParamValue("res", "succ_update") ;
				retxd.setSubDataSingle("item", si.toXmlData()) ;
			}
			else if(si.updateRes==SynItem.UpdateRes.delete)
			{
				retxd.setParamValue("res", "succ_delete") ;
				retxd.setParamValue("pkid", si.pkId) ;
			}
			else
			{
				retxd.setParamValue("res", "succ") ;
			}
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("sst_delete"))
		{
			String pkid = xd.getParamValueStr("pkid") ;
			XmlData tmpxd = xd.getSubDataSingle("update_xd");
			boolean b = ist.SST_onClientDelete(clientid, pkid);
			if(b)
			{
				XmlData retxd = new XmlData() ;
				retxd.setParamValue("res", "succ") ;
				retxd.writeOutCompact(resp_out,"utf-8") ;
			}
			return ;
		}
	}
	
	public void synInJsp(String clientid,String msgtype,InputStream req_in,OutputStream resp_out)
		throws Exception
	{
		XmlData xd = XmlData.parseFromStream(req_in,"utf-8") ;
		if(xd==null)
			return ;
		
		String tablen = xd.getParamValueStr("table_name") ;
		if(Convert.isNullOrEmpty(tablen))
			return ;
		
		ISynClientable ist = getSynTable(tablen);
		if(ist==null)
			return ;
		
		String coln = ist.getSynClientColByTableName(tablen) ;
		String server_tablen = clientTN2ServerTn.get(tablen) ;
		if(msgtype.equals("gdb_client_syn_struct"))
		{
			XmlDataStruct xds = ist.getSynClientStruct(coln);
			if(xds==null)
				return ;
			xds.setName(tablen) ;
			
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			retxd.setSubDataSingle("xds", xds.toXmlData()) ;
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("gdb_client_syn_table_info"))
		{
			XmlDataStruct xds = ist.getSynClientStruct(coln);
			if(xds==null)
				return ;
			xds.setName(tablen) ;
			
			XmlData tableinfo_xd = new XmlData() ;
			XmlData xds_xd = xds.toXmlData();
			
			tableinfo_xd.setSubDataSingle("xds", xds_xd) ;
			tableinfo_xd.setParamValue("max_log_dt", getSynClientMaxLogDT(server_tablen,coln,clientid)) ;
			tableinfo_xd.setParamValue("record_num", ist.getSynClientTableRecordNum(coln,clientid)) ;
			
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			retxd.setSubDataSingle("table_info", tableinfo_xd) ;
			//System.out.println(tableinfo_xd.toXmlString());
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("gdb_client_syn_read_after_pk"))
		{
			int readn = xd.getParamValueInt32("read_num", 20) ;
			String lastid = xd.getParamValueStr("last_max_pk") ;
			List<XmlData> dataxd = ist.getSynClientData(coln,clientid, lastid, readn) ;
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			if(dataxd!=null)
				retxd.setSubDataArray("records", dataxd) ;
			//System.out.println(retxd.toXmlString());
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("gdb_client_syn_del_old_log"))
		{
			long max_logdt = xd.getParamValueInt64("max_log_dt", -1) ;
			
			delSynClientOldChgLog(server_tablen,coln,clientid,max_logdt) ;
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("gdb_client_syn_read_by_log"))
		{
			int readn = xd.getParamValueInt32("read_num", 20) ;
			HashMap<String,Long> id2dt = readChgLog(server_tablen,coln,clientid,readn);
			List<XmlData> dataxd = null;
			XmlData id2dt_xd = null ;
			if(id2dt!=null&&id2dt.size()>0)
			{
				id2dt_xd = new XmlData() ;
				List<String> pkids = new ArrayList<String>(id2dt.size()) ;
				for(Map.Entry<String,Long> iddt:id2dt.entrySet())
				{
					pkids.add(iddt.getKey()) ;
					id2dt_xd.setParamValue(iddt.getKey(), iddt.getValue());
				}
				dataxd = ist.getSynClientData(coln,clientid, pkids);
			}
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			if(id2dt_xd!=null)
			{
				retxd.setSubDataSingle("id2dt", id2dt_xd) ;
				retxd.setSubDataArray("records", dataxd) ;
			}
			
			//System.out.println("gdb_client_syn_read_by_log client_tn="+ist.getSynClientTableName(clientid));
			//System.out.println(retxd.toXmlString());
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("gdb_client_syn_del_log_id2dt"))
		{
			XmlData id2dt_xd = xd.getSubDataSingle("id2dt") ;
			if(id2dt_xd==null||id2dt_xd.getParamNum()<=0)
				return ;
			
			for(String pkid:id2dt_xd.getParamNames())
			{
				long dt = id2dt_xd.getParamValueInt64(pkid, -1);
				if(dt<=0)
					continue ;
				
				delSynClientChgLog(server_tablen,coln,clientid,pkid,dt);
			}
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("res", "succ") ;
			retxd.writeOutCompact(resp_out,"utf-8") ;
			return ;
		}
		else if(msgtype.equals("gdb_client_syn_add"))
		{
			XmlData add_xd = xd.getSubDataSingle("add_xd") ;
			if(add_xd==null||add_xd.getParamNum()<=0)
				return ;
			
			XmlData rxd = ist.onSynClientAdd(coln, clientid, add_xd);
			if(rxd!=null)
			{
				XmlData retxd = new XmlData() ;
				retxd.setParamValue("res", "succ") ;
				retxd.setSubDataSingle("ret", rxd) ;
				retxd.writeOutCompact(resp_out,"utf-8") ;
				return ;
			}
		}
		else if(msgtype.equals("gdb_client_syn_update"))
		{
			
		}
		else if(msgtype.equals("gdb_client_syn_delete"))
		{
			
		}
	}
}
