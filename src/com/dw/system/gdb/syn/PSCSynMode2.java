package com.dw.system.gdb.syn;

import java.io.File;

import com.dw.system.AppConfig;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.conf.autofit.JavaTableInfo;
import com.dw.system.gdb.connpool.GDBConn;
import com.dw.system.gdb.syn.ProxyServerCmd.ProxySentRes;
import com.dw.system.xmldata.XmlData;

/**
 * 模式2的同步，更新只通过Server端
 * 
 * 同步模式采用DataTable下载更新
 * 
 * Server端对表的更新，都会记录一个时间戳。如果Proxy端的时间戳与Proxy对应的时间戳不一致
 * 则，做下载更新。
 * 
 * 下载更新过程中，需要提供每次更新记录数，Proxy端需要多次调用本对象的发送方法。并且不断提高id值
 * 每次返回的DataTable表中，id也是按照顺序，这样就可以做DataTable表中的insert,update和delete
 * 三种本地操作。
 * 
 * Server端的表有可能是个虚拟表，只需要XORM类实现接口{@see ISynVirtualServerTable}
 * 则Server端对应的XORM类的表就不会建立。
 * 
 * 客户端下载内容时，Server通过ISynVirtualServerTable的接口实现返回数据。
 * 
 * @author Jason Zhu
 */
public class PSCSynMode2 extends ProxyServerCmd
{
	/**
	 * 根据数据库表名词，获得对应的更新时间戳
	 * @param tablen
	 * @return
	 */
	static long Proxy_readTimestamp(String tablen)
	{
		String fp = AppConfig.getDataDirBase()+"proxy_m2_"+tablen.toLowerCase()+".xml" ;
		File f = new File(fp) ;
		if(!f.exists())
			return -1 ;
		
		try
		{
			XmlData xd = XmlData.readFromFile(f) ;
			return xd.getParamValueInt64("time_stamp",-1) ;
		}
		catch(Exception ee)
		{
			return -1 ;
		}
	}
	
	/**
	 * 根据数据库表名称，保存对应的时间戳
	 * @param tablen
	 * @param ts
	 */
	static void Proxy_saveTimestamp(String tablen,long ts)
	{
		String fp = AppConfig.getDataDirBase()+"proxy_m2_"+tablen.toLowerCase()+".xml" ;
		File f = new File(fp) ;
		XmlData xd = null ;
		try
		{
			if(f.exists())
				xd = XmlData.readFromFile(f) ;
			else
				xd = new XmlData() ;
			
			xd.setParamValue("time_stamp",ts) ;
			
			XmlData.writeToFile(xd, f) ;
		}
		catch(Exception ee)
		{
			//return -1 ;
		}
	}
	
	
	
	String tableName = null ;
	
	int readEveryTime = 20 ;
	
	Class xormC = null ;
	JavaTableInfo jti = null ;
	
	PSCSynMode2()
	{}
	
	public PSCSynMode2(String tablen,int read_et) throws ClassNotFoundException
	{
		this.tableName = tablen ;
		readEveryTime = read_et ;
		if(readEveryTime<=0)
			readEveryTime = 20 ;
		
		xormC = GDBLogManager.getInstance().getXORMClassByTableName(tableName) ;
		jti = Gdb.getXORMByGlobal(xormC).getJavaTableInfo() ;
	}
	
	@Override
	public String getCmdName()
	{
		return "syn_mode2";
	}
	
	boolean b_check = true ;
	Object lastMaxPk = null ;

	@Override
	protected XmlData Proxy_getMsgToBeSent() throws Exception
	{
		XmlData xd = new XmlData() ;
		xd.setParamValue("table", tableName) ;
		
		xd.setParamValue("read_num", readEveryTime) ;
		if(lastMaxPk!=null)
			xd.setParamValue("last_max_pk", lastMaxPk) ;
		
		return xd;
	}

	@Override
	protected ProxySentRes Proxy_onResponseSucc(XmlData resp_xd) throws Exception
	{
		DataTable dt = new DataTable() ;
		dt.fromXmlData(resp_xd) ;
		
		System.out.println("syn mode2 recv dt====\n"+dt.toString()) ;
//		更新如本地数据库
		int rn = dt.getRowNum() ;
		if(rn>0)
		{
			String pkcol = jti.getPkColumnInfo().getColumnName() ;
			GDB.getInstance().synDataTableToXORMPKUniqueOrder(xormC, dt, false);
			lastMaxPk = dt.getRow(rn-1).getValue(pkcol) ;
		}
		
		if(rn<readEveryTime)
		{
			return ProxySentRes.succ_normal ;
		}
		else
		{
			return ProxySentRes.succ_repeat ;
		}
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
		String tablen = xd.getParamValueStr("table") ;
		Object maxpk = xd.getParamValue("last_max_pk") ;
		int rn = xd.getParamValueInt32("read_num", 20) ;
		
		Class xormc = GDBLogManager.getInstance().getXORMClassByTableName(tablen) ;//lt.getXormClass() ;
		
		DataTable dt = null ;
		if(ISynVirtualServerTable.class.isAssignableFrom(xormc))
		{
			ISynVirtualServerTable svst = (ISynVirtualServerTable)xormc.newInstance() ;
			dt = svst.readSynUpdateDT(proxyid, maxpk, rn) ;
		}
		else
		{
			XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
			JavaTableInfo jti = xorm_conf.getJavaTableInfo() ;
			String pk_coln = jti.getPkColumnInfo().getColumnName() ;
			
			String wherestr = null ;
			if(maxpk!=null)
			{
				if(maxpk instanceof Number)
				{
					wherestr = pk_coln+">"+maxpk.toString() ;
				}
				else
				{
					wherestr = pk_coln+">'"+maxpk.toString()+"'";
				}
			}
			dt = GDB.getInstance().listXORMAsTable(xormc, wherestr, pk_coln, 0, rn);
		}
		
		if(dt==null)
			return null ;
		
		System.out.println("maxpk=="+maxpk+"\n  "+dt.toString());
		return dt.toXmlData();
	}
}
