package com.dw.system.gdb.syn;

import com.dw.system.gdb.*;
import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.conf.autofit.JavaTableInfo;
import com.dw.system.gdb.connpool.*;
import com.dw.system.xmldata.*;

/**
 * 本指令支持如下情况
 * 当一个Proxy由于维护等原因，重新进行了安装，对应Mode1的表需要从Server端获取
 * 原有的数据
 * 此时，需要通过本指令进行内容的下载
 * 
 * 另外，对于模式2的分布式表，由于更新都在服务器端。在初始的时候，也需要对此进行同步
 * 
 * Proxy发起请求，
 * @author Jason Zhu
 */
public class PSCSynMode1Init extends ProxyServerCmd
{
	GDBLogTable logTable = null ;
	int readNum = -1 ;
	
	PSCSynMode1Init()
	{}
	
	public PSCSynMode1Init(GDBLogTable lt,int readnum)
	{
		logTable = lt ;
		readNum = readnum ;
		if(readNum<=0)
			readNum = 20 ;
	}
	
	@Override
	public String getCmdName()
	{
		return "syn_mode1_init";
	}

	
	@Override
	protected XmlData Proxy_getMsgToBeSent() throws Exception
	{
		Object maxpk = GDB.getInstance().getMaxPkIdByXORM(logTable.xormC);
		if(maxpk==null)
			maxpk = "00";
		
		XmlData xd = new XmlData() ;
		xd.setParamValue("p_maxpk", maxpk) ;
		xd.setParamValue("read_num", readNum) ;
		xd.setParamValue("table", logTable.getTableName()) ;
		return xd;
	}

	
	@Override
	protected ProxySentRes Proxy_onResponseSucc(XmlData resp_xd) throws Exception
	{
		if(resp_xd==null)
			return ProxySentRes.succ_normal;
		
		DataTable dt = new DataTable("t0") ;
		dt.fromXmlData(resp_xd) ;
		
		//根据表中的内容插入到数据库中
		GDB.getInstance().setOrUpdateXORMWithDataTable(logTable.xormC,dt,false);
		
		int rn = dt.getRowNum() ;
		if(rn<readNum)
			return ProxySentRes.succ_normal ;
		else
			return ProxySentRes.succ_repeat ;
	}

	
	@Override
	protected ProxySentRes Proxy_onResponseError(Exception error)
	{
		return ProxySentRes.error_normal;
	}

	
	@Override
	protected ProxySentRes Proxy_onRequestError(Exception error)
	{
		return ProxySentRes.error_normal;
	}

	
	@Override
	protected boolean Server_needDBConn()
	{
		return false;
	}

	@Override
	protected XmlData Server_onRequestRecved(GDBConn conn, String proxyid, XmlData xd) throws Exception
	{
		String maxpk = xd.getParamValueStr("p_maxpk") ;
		int readNum = xd.getParamValueInt32("read_num", 20) ;
		String tablen = xd.getParamValueStr("table") ;
		//Class c = Class.forName(xormcn) ;
		
		//GDBLogTable lt = GDBLogManager.getInstance().getLogTable(tablen) ;
		Class xormc = GDBLogManager.getInstance().getXORMClassByTableName(tablen) ;//lt.getXormClass() ;
		XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
		JavaTableInfo jti = xorm_conf.getJavaTableInfo() ;
		String pk_coln = jti.getPkColumnInfo().getColumnName() ;
		
		
		DataTable dt = GDB.getInstance().listXORMAsTable(xormc, pk_coln+" like '"+proxyid+"%' and "+pk_coln+">'"+maxpk+"'", pk_coln, 0, readNum);
		
		return dt.toXmlData();
	}

}
