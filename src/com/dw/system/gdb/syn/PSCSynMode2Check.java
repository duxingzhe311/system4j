package com.dw.system.gdb.syn;

import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.conf.autofit.JavaTableInfo;
import com.dw.system.gdb.connpool.GDBConn;
import com.dw.system.gdb.syn.ProxyServerCmd.ProxySentRes;
import com.dw.system.xmldata.XmlData;

/**
 * 模式2情况下Proxy向Server发起同步检测
 * @author Jason Zhu
 *
 */
public class PSCSynMode2Check extends ProxyServerCmd
{
	String tableName = null ;
	
	PSCSynMode2Check()
	{}
	
	public PSCSynMode2Check(String tablen)
	{
		tableName = tablen ;
	}
	
	@Override
	public String getCmdName()
	{
		return "syn_mode2_check";
	}

	@Override
	protected XmlData Proxy_getMsgToBeSent() throws Exception
	{
		XmlData xd = new XmlData() ;
		long t = PSCSynMode2.Proxy_readTimestamp(tableName);
		//xd.setParamValue("check_time", xd)
		xd.setParamValue("table", tableName) ;
		xd.setParamValue("time_stamp", t);
		return xd ;
	}
	
	transient boolean checkSucc = false;
	transient long serverTimestamp = -1 ;
	
	boolean isCheckSucc()
	{
		return checkSucc ;
	}
	
	long getServerTimestamp()
	{
		return serverTimestamp ;
	}

	@Override
	protected ProxySentRes Proxy_onResponseSucc(XmlData resp_xd) throws Exception
	{
		checkSucc = true;
		serverTimestamp = resp_xd.getParamValueInt64("time_stamp", -1) ;
		return ProxySentRes.succ_normal;
	}

	@Override
	protected ProxySentRes Proxy_onResponseError(Exception error)
	{
//		记录错误
		return ProxySentRes.error_normal ;
	}

	@Override
	protected ProxySentRes Proxy_onRequestError(Exception error)
	{
//		记录错误
		return ProxySentRes.error_normal ;
	}

	@Override
	protected boolean Server_needDBConn()
	{
		return false;
	}

	@Override
	protected XmlData Server_onRequestRecved(GDBConn conn, String proxyid, XmlData xd) throws Exception
	{
		long t = xd.getParamValueInt64("time_stamp", -1);
		String tablen = xd.getParamValueStr("table") ;
		
		Class xormc = GDBLogManager.getInstance().getXORMClassByTableName(tablen) ;//lt.getXormClass() ;
		
		DataTable dt = null ;
		if(ISynVirtualServerTable.class.isAssignableFrom(xormc))
		{
			ISynVirtualServerTable svst = (ISynVirtualServerTable)xormc.newInstance() ;
			long slt = svst.getSynUpdateTimestamp(proxyid) ;
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("time_stamp", slt) ;
			return retxd ;
		}
		else
		{
			XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
			JavaTableInfo jti = xorm_conf.getJavaTableInfo() ;
			String pk_coln = jti.getPkColumnInfo().getColumnName() ;
			
			GDBLogTable lt = GDBLogManager.getInstance().getLogTable(tablen);
			long maxdt = lt.getMaxLogDT() ;
			//0-表示没有记录
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("time_stamp", maxdt) ;
			return retxd ;
		}
	}
}
