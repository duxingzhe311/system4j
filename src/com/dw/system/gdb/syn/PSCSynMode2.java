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
 * ģʽ2��ͬ��������ֻͨ��Server��
 * 
 * ͬ��ģʽ����DataTable���ظ���
 * 
 * Server�˶Ա�ĸ��£������¼һ��ʱ��������Proxy�˵�ʱ�����Proxy��Ӧ��ʱ�����һ��
 * �������ظ��¡�
 * 
 * ���ظ��¹����У���Ҫ�ṩÿ�θ��¼�¼����Proxy����Ҫ��ε��ñ�����ķ��ͷ��������Ҳ������idֵ
 * ÿ�η��ص�DataTable���У�idҲ�ǰ���˳�������Ϳ�����DataTable���е�insert,update��delete
 * ���ֱ��ز�����
 * 
 * Server�˵ı��п����Ǹ������ֻ��ҪXORM��ʵ�ֽӿ�{@see ISynVirtualServerTable}
 * ��Server�˶�Ӧ��XORM��ı�Ͳ��Ὠ����
 * 
 * �ͻ�����������ʱ��Serverͨ��ISynVirtualServerTable�Ľӿ�ʵ�ַ������ݡ�
 * 
 * @author Jason Zhu
 */
public class PSCSynMode2 extends ProxyServerCmd
{
	/**
	 * �������ݿ�����ʣ���ö�Ӧ�ĸ���ʱ���
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
	 * �������ݿ�����ƣ������Ӧ��ʱ���
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
//		�����籾�����ݿ�
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
